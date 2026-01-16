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

      def akka: ConfigFunc = helpers.Akka.configure

      def Akka = helpers.Akka

      /** Use this to provide dependencies on most recent Akka libraries */

      /** Use this to provide handy sbt command line aliases */
      def aliases: ConfigFunc = helpers.HandyAliases.configure

      /** Use this to configure AsciiDoc document generation for static websites and PDFs */
      val AsciiDoc = helpers.AsciiDoc

      /** Use this to have the build generate build information. "I know this because sbt knows
        * this"
        */
      def build_info: ConfigFunc = helpers.BuildInfo.configure

      def BuildInfo: helpers.BuildInfo.type = helpers.BuildInfo

      /** Configure the project to require a certain percentage of coverage in test cases */
      def coverage(percent: Double = 50.0d)(project: Project): Project =
        project
          .configure(helpers.ScalaCoverage.configure)
          .settings(
            helpers.ScalaCoverage.Keys.coveragePercent := percent
          )

      /** Use dynamic versioning based on the most recent tag, and the commit hash and data/time
        * stamp if necessary
        */
      def dynver: ConfigFunc = helpers.DynamicVersioning.configure

      /** Use this to get git command line support at the sbt prompt */
      def git: ConfigFunc = helpers.Git.configure

      /** Configure this project to be published as a Maven GitHub Package in the organization
        * specified by Root
        * @note
        *   Do not combine this with SonatypePublishing
        */
      def GithubPublishing: ConfigFunc = helpers.GithubPublishing.configure

      /** Use this to get the `headerCheck` and `headerCreate` sbt commands to generate source file
        * headers automatically
        */
      def header: ConfigFunc = helpers.Header.configure

      def IdeaPlugin: helpers.IdeaPlugin.type = helpers.IdeaPlugin

      /** Use this to enable compilation of Java code too */
      def java: ConfigFunc = helpers.Java.configure

      /** Use this to configure your project to compile Scala to ScalaJS via scala.js */
      def ScalaJS: helpers.ScalaJS.type = helpers.ScalaJS
      def scalajs: ConfigFunc = helpers.ScalaJS.configure

      /** @deprecated Use ScalaJS instead - this alias will be removed in 2.0 */
      @deprecated("Use ScalaJS instead", "1.0.0")
      def Javascript: helpers.ScalaJS.type = helpers.ScalaJS

      /** Use this to configure your project to include typical laminar dependencies */
      def Laminar: helpers.Laminar.type = helpers.Laminar

      def MiMa: helpers.MiMa.type = helpers.MiMa

      /** Use this to configure your project to build native code with Scala.Native */
      def Native: helpers.Native.type = helpers.Native

      /** Do not configure this project for Lightbend's Migration Manager */
      def noMiMa: ConfigFunc = helpers.MiMa.without

      /** Configure this project to produce no artifact and not be published */
      def noPublishing(project: Project): Project =
        project.settings(
          publishArtifact := false, // no artifact to publish for the virtual root project
          publish := {}, // just to be sure
          publishLocal := {}, // and paranoid
          publishTo := Some(Resolver.defaultLocal),
          publish / skip := true
        )

      def Packaging: helpers.Packaging.type = helpers.Packaging

      /** Configure this project to be published as open source
        * @note
        *   Do not combine this with SonatypePublishing
        */
      def SonatypePublishing: ConfigFunc = helpers.SonatypePublishing.configure

      /** Configure this project to support releasing with a systematic release procedure */
      def release: ConfigFunc = helpers.Release.configure

      /** Add extra resolvers to the build for this project */
      def resolvers: ConfigFunc = helpers.Resolvers.configure

      /** Configure dependency on a version of the RIDDL library */
      def riddl: ConfigFunc = helpers.Riddl.configure
      def Riddl = helpers.Riddl

      /** Compile scala code as Scala 2.13.latest */
      def scala2: ConfigFunc = helpers.Scala2.configure

      /** Compile scala code as Scala 3's latest LTS release */
      def scala3: ConfigFunc = helpers.Scala3.configure

      /** Configure this project to use standard Scalafmt formatting rules. */
      def Scalafmt: helpers.Scalafmt.type = helpers.Scalafmt

      /** Add scalaTest libraries to the libraryDependencies */
      def Scalatest: helpers.Scalatest.type = helpers.Scalatest

      /** Configure this project to enable coverage testing */
      def scoverage: ConfigFunc = helpers.ScalaCoverage.configure

      def ScalablyTyped: helpers.ScalablyTyped.type = helpers.ScalablyTyped

      def Unidoc: helpers.Unidoc.type = helpers.Unidoc

      def these(cfuncs: ConfigFunc*)(project: Project): Project =
        cfuncs.foldLeft(project) { (p, func) =>
          p.configure(func)
        }

      /** Use this to more easily configure:
        *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.aliases]],
        *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.dynver]]
        *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.git]]
        *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.header]]
        *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.resolvers]] more easily
        */
      def basic(project: Project): Project =
        these(aliases, dynver, git, header, resolvers)(project)

      /** Configure support for all the simple things:
        *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.typical]]
        *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.java]]
        *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.build_info]]
        *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.release]]
        */
      def everything(project: Project): Project = {
        project.configure(typical)
        these(java, release)(project)
      }

      /** Use this to enable the [[basic]] features as well as [[scala3]] and [[build_info]] */
      def typical(project: Project): Project = {
        project
          .configure(basic)
          .configure(scala3)
          .configure(Scalatest())
      }
    }
  }

  override def projectSettings: Seq[Setting[_]] = Nil
}
