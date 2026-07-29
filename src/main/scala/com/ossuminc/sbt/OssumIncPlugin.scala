package com.ossuminc.sbt

import sbt.*
import sbt.Keys.*
import sbt.librarymanagement.Resolver

/** And sbt plugin for many different kind of projects and used for every project at Ossum Inc.
  */
object OssumIncPlugin extends AutoPlugin {

  object autoImport {
    // The type of function that `Project.configure` takes as its argument
    private type ConfigFunc = Project => Project

    // Major declarations
    val Root: com.ossuminc.sbt.Root.type = com.ossuminc.sbt.Root
    val Module: com.ossuminc.sbt.Module.type = com.ossuminc.sbt.Module
    val Plugin: com.ossuminc.sbt.Plugin.type = com.ossuminc.sbt.Plugin
    val Program: com.ossuminc.sbt.Program.type = com.ossuminc.sbt.Program
    val CrossModule: com.ossuminc.sbt.CrossModule.type = com.ossuminc.sbt.CrossModule
    val DocSite: com.ossuminc.sbt.DocSite.type = com.ossuminc.sbt.DocSite

    // Version classification. `isSnapshot` deliberately does NOT answer this:
    // for an RC tag (1.32.0-rc.1) dynver reports isSnapshot=false, correctly,
    // because an RC is a real immutable artifact -- which is exactly why it
    // cannot be used as a proxy for "is this a prerelease?". Without these,
    // every consumer re-derives the answer by string-matching the version.
    val isPrerelease: SettingKey[Boolean] =
      helpers.DynamicVersioning.Keys.isPrerelease
    val isReleaseCandidate: SettingKey[Boolean] =
      helpers.DynamicVersioning.Keys.isReleaseCandidate

    // Kinds of cross-builds supported by CrossModule
    val JVM: CrossModule.Target = CrossModule.JVMTarget
    val JS: CrossModule.Target = CrossModule.JSTarget
    val Native: CrossModule.Target = CrossModule.NativeTarget

    // Clauses to customize the major declarations
    object With {

      // ===== PascalCase helpers (preferred) =====

      /** Use this to provide dependencies on most recent Akka libraries */
      def Akka: helpers.Akka.type = helpers.Akka

      /** Use this to provide handy sbt command line aliases */
      def Aliases: helpers.HandyAliases.type = helpers.HandyAliases

      /** Use this to configure AsciiDoc document generation */
      val AsciiDoc: helpers.AsciiDoc.type = helpers.AsciiDoc

      /** Use this to have the build generate build information */
      def BuildInfo: helpers.BuildInfo.type = helpers.BuildInfo

      /** Use dynamic versioning based on git tags */
      def DynVer: helpers.DynamicVersioning.type = helpers.DynamicVersioning

      /** Use this to get git command line support at the sbt prompt */
      def Git: helpers.Git.type = helpers.Git

      /** Configure publishing to GitHub Packages
        * @note
        *   Do not combine with SonatypePublishing
        */
      def GithubPublishing: helpers.GithubPublishing.type = helpers.GithubPublishing

      /** Use this to manage source file headers automatically */
      def Header: helpers.Header.type = helpers.Header

      /** Configure IntelliJ IDEA plugin development */
      def IdeaPlugin: helpers.IdeaPlugin.type = helpers.IdeaPlugin

      /** Use this to enable compilation of Java code too */
      def Java: helpers.Java.type = helpers.Java

      /** Use this to configure Laminar dependencies */
      def Laminar: helpers.Laminar.type = helpers.Laminar

      /** Configure binary compatibility checking */
      def MiMa: helpers.MiMa.type = helpers.MiMa

      /** Use this to build native code with Scala Native */
      def Native: helpers.Native.type = helpers.Native

      /** Configure sbt-native-packager */
      def Packaging: helpers.Packaging.type = helpers.Packaging

      /** Configure artifact publishing (defaults to GitHub Packages)
        *
        * Use `With.Publishing` for default (GitHub), or explicitly:
        * - `With.Publishing.github` for GitHub Packages
        * - `With.Publishing.sonatype` for Sonatype/Maven Central
        */
      def Publishing: helpers.Publishing.type = helpers.Publishing

      /** Configure the release process */
      def Release: helpers.Release.type = helpers.Release

      /** Add extra resolvers to the build */
      def Resolvers: helpers.Resolvers.type = helpers.Resolvers

      /** Configure dependency on RIDDL library */
      def Riddl: helpers.Riddl.type = helpers.Riddl

      /** Compile scala code as Scala 2.13.latest */
      def Scala2: helpers.Scala2.type = helpers.Scala2

      /** Compile scala code as Scala 3's latest LTS release */
      def Scala3: helpers.Scala3.type = helpers.Scala3

      /** Configure code coverage testing */
      def ScalaCoverage: helpers.ScalaCoverage.type = helpers.ScalaCoverage

      /** Configure standard Scalafmt formatting rules */
      def Scalafmt: helpers.Scalafmt.type = helpers.Scalafmt

      /** Configure Scala.js compilation */
      def ScalaJS: helpers.ScalaJS.type = helpers.ScalaJS

      /** Add scala-java-time dependency for cross-platform java.time API */
      def ScalaJavaTime: helpers.ScalaJavaTime.type = helpers.ScalaJavaTime

      /** Add ScalaTest libraries to the libraryDependencies */
      def Scalatest: helpers.Scalatest.type = helpers.Scalatest

      /** Configure publishing to Sonatype/Maven Central
        * @note
        *   Do not combine with GithubPublishing
        */
      def SonatypePublishing: helpers.SonatypePublishing.type = helpers.SonatypePublishing

      /** Configure unified API documentation */
      def Unidoc: helpers.Unidoc.type = helpers.Unidoc

      // ===== Special helpers =====

      /** Configure code coverage with minimum threshold */
      def coverage(percent: Double = 50.0d)(project: Project): Project =
        project
          .configure(helpers.ScalaCoverage)
          .settings(
            helpers.ScalaCoverage.Keys.coveragePercent := percent
          )

      /** Do not configure binary compatibility checking */
      def noMiMa: ConfigFunc = helpers.MiMa.without

      /** Use a classpath JAR for packaging (reduces command line length) */
      def ClassPathJar: ConfigFunc = helpers.Miscellaneous.useClassPathJar

      /** Use unmanaged JAR files from libs/ directory */
      def UnmanagedJars: ConfigFunc = helpers.Miscellaneous.useUnmanagedJarLibs

      /** Custom shell prompt showing project name, branch, and version */
      def ShellPrompt: Def.Initialize[State => String] =
        helpers.Miscellaneous.buildShellPrompt

      /** Configure project to produce no artifact and not be published */
      def noPublishing(project: Project): Project =
        project.settings(
          publishArtifact := false,
          publish := {},
          publishLocal := {},
          publishTo := Some(Resolver.defaultLocal),
          publish / skip := true
        )

      /** Suppress Scaladoc/Javadoc generation for this module by clearing the
        * `doc` task's input sources. Useful where doc generation is slow or
        * unnecessary. The (now empty) `packageDoc` artifact is still produced,
        * so Sonatype's javadoc-jar publishing requirement remains satisfied.
        */
      def NoDocs(project: Project): Project =
        project.settings(
          Compile / doc / sources := Seq.empty
        )

      /** Apply multiple configuration functions */
      def these(cfuncs: ConfigFunc*)(project: Project): Project =
        cfuncs.foldLeft(project) { (p, func) =>
          p.configure(func)
        }

      // ===== Composite helpers =====

      /** Configure: Aliases, DynVer, Git, Header, Resolvers */
      def basic(project: Project): Project =
        these(Aliases, DynVer, Git, Header, Resolvers)(project)

      /** Configure: basic + Scala3 + Scalatest */
      def typical(project: Project): Project = {
        project
          .configure(basic)
          .configure(Scala3)
          .configure(Scalatest)
      }

      /** Configure: typical + Java + Release */
      def everything(project: Project): Project = {
        project.configure(typical)
        these(Java, Release)(project)
      }
    }
  }

  // sbt orders an AutoPlugin's projectSettings BELOW everything written in
  // build.sbt, which is exactly what a "default the project may override"
  // needs: the override wins wherever the author writes it, rather than only
  // when it happens to follow the .configure(...) call that set the key.
  override def projectSettings: Seq[Setting[?]] =
    helpers.NpmPackaging.defaultSettings ++ helpers.NpmPublishing.defaultSettings

  override def buildSettings: Seq[Setting[?]] = Seq(
    // Provide default (sentinel) values for RootProjectInfo keys
    // These will be overridden when Root() is called
    helpers.RootProjectInfo.Keys.gitHubOrganization :=
      helpers.RootProjectInfo.NOT_CONFIGURED,
    helpers.RootProjectInfo.Keys.gitHubRepository :=
      helpers.RootProjectInfo.NOT_CONFIGURED,
    helpers.RootProjectInfo.Keys.copyrightHolder :=
      helpers.RootProjectInfo.NOT_CONFIGURED
  )
}
