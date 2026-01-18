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

      /** Configure ScalablyTyped for TypeScript facades */
      def ScalablyTyped: helpers.ScalablyTyped.type = helpers.ScalablyTyped

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

      // ===== Deprecated lowercase aliases (will be removed in 2.0) =====

      @deprecated("Use Akka instead", "1.1.0")
      def akka: helpers.Akka.type = helpers.Akka

      @deprecated("Use Aliases instead", "1.1.0")
      def aliases: helpers.HandyAliases.type = helpers.HandyAliases

      @deprecated("Use BuildInfo instead", "1.1.0")
      def build_info: helpers.BuildInfo.type = helpers.BuildInfo

      @deprecated("Use DynVer instead", "1.1.0")
      def dynver: helpers.DynamicVersioning.type = helpers.DynamicVersioning

      @deprecated("Use Git instead", "1.1.0")
      def git: helpers.Git.type = helpers.Git

      @deprecated("Use Header instead", "1.1.0")
      def header: helpers.Header.type = helpers.Header

      @deprecated("Use Java instead", "1.1.0")
      def java: helpers.Java.type = helpers.Java

      @deprecated("Use ScalaJS instead", "1.1.0")
      def Javascript: helpers.ScalaJS.type = helpers.ScalaJS

      @deprecated("Use Release instead", "1.1.0")
      def release: helpers.Release.type = helpers.Release

      @deprecated("Use Resolvers instead", "1.1.0")
      def resolvers: helpers.Resolvers.type = helpers.Resolvers

      @deprecated("Use Riddl instead", "1.1.0")
      def riddl: helpers.Riddl.type = helpers.Riddl

      @deprecated("Use Scala2 instead", "1.1.0")
      def scala2: helpers.Scala2.type = helpers.Scala2

      @deprecated("Use Scala3 instead", "1.1.0")
      def scala3: helpers.Scala3.type = helpers.Scala3

      @deprecated("Use ScalaJS instead", "1.1.0")
      def scalajs: helpers.ScalaJS.type = helpers.ScalaJS

      @deprecated("Use ScalaCoverage instead", "1.1.0")
      def scoverage: helpers.ScalaCoverage.type = helpers.ScalaCoverage

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

  override def projectSettings: Seq[Setting[_]] = Nil

  override def buildSettings: Seq[Setting[_]] = Seq(
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
