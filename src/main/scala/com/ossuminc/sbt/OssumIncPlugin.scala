package com.ossuminc.sbt

import sbt.*
import sbt.Keys.*
import sbt.librarymanagement.Resolver

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
    val JVM: CrossModule.Target = CrossModule.JVMTarget
    val JS: CrossModule.Target = CrossModule.JSTarget
    val Native: CrossModule.Target = CrossModule.NativeTarget

    // Clauses to customize the major declarations
    object With {
      val akka: ConfigFunc = helpers.Akka.configure
      val aliases: ConfigFunc = helpers.HandyAliases.configure
      val build_info: ConfigFunc = helpers.BuildInfo.configure
      val dynver: ConfigFunc = helpers.DynamicVersioning.configure
      val git: ConfigFunc = helpers.Git.configure
      val header: ConfigFunc = helpers.Header.configure
      val java: ConfigFunc = helpers.Java.configure
      val javascript: ConfigFunc = helpers.Javascript.configure
      val misc: ConfigFunc = helpers.Miscellaneous.configure
      val laminar: ConfigFunc = helpers.Laminar.configure
      val native: ConfigFunc = helpers.Native.configure
      val publishing: ConfigFunc = helpers.SonatypePublishing.configure
      val release: ConfigFunc = helpers.Release.configure
      val resolvers: ConfigFunc = helpers.Resolvers.configure
      val scala2: ConfigFunc = helpers.Scala2.configure
      val scala3: ConfigFunc = helpers.Scala3.configure
      val scalaTest: ConfigFunc = helpers.ScalaTest.configure
      val scalafmt: ConfigFunc = helpers.Scalafmt.configure
      val scoverage: ConfigFunc = helpers.ScalaCoverage.configure

      def these(cfuncs: ConfigFunc*)(project: Project): Project = {
        cfuncs.foldLeft(project) { (p, func) =>
          p.configure(func)
        }
      }

      def basic(project: Project): Project = {
        these(aliases, dynver, git, header, resolvers)(project)
      }

      def typical(project: Project): Project = {
        project.configure(basic)
        these(scala3, scalaTest, publishing)(project)
      }

      def everything(project: Project): Project = {
        project.configure(typical)
        these(java, misc, build_info, release)(project)
      }

      def unidoc(
        apiOutput: File = file("target/unidoc"),
        baseURL: Option[String] = None,
        inclusions: Seq[ProjectReference] = Seq.empty,
        exclusions: Seq[ProjectReference] = Seq.empty,
        logoPath: Option[String] = None,
        externalMappings: Seq[Seq[String]] = Seq.empty
      )(project: Project): Project = {
        project
          .configure(helpers.Unidoc.configure(apiOutput, baseURL, inclusions,exclusions, logoPath, externalMappings))
      }


      def noPublishing(project: Project): Project = {
        project.settings(
          publishArtifact := false, // no artifact to publish for the virtual root project
          publish := {}, // just to be sure
          publishLocal := {}, // and paranoid
          publishTo := Some(Resolver.defaultLocal),
          publish / skip := true
        )
      }

      def coverage(percent: Double = 50.0d)(project: Project): Project = {
        project
          .configure(helpers.ScalaCoverage.configure)
          .settings(
            helpers.ScalaCoverage.Keys.coveragePercent := percent
          )
      }

      def js(
        header: String = "no header",
        hasMain: Boolean = false,
        forProd: Boolean = true,
        withCommonJSModule: Boolean = false
      )(project: Project): Project = {
        helpers.Javascript.configure(header, hasMain, forProd, withCommonJSModule)(project)
      }

      def native(
        buildTarget: String = "static",
        targetTriple: String = "arm64-apple-macosx11.0.0",
        gc: String = "commix",
        debug: Boolean = true,
        noLTO: Boolean = false,
        debugLog: Boolean = false,
        verbose: Boolean = false,
        ld64Path: String = "/opt/homebrew/opt/llvm/bin/ld64.lld"
      )(project: Project): Project = {
        helpers.Native.configure(
          buildTarget,
          targetTriple,
          gc,
          debug,
          noLTO,
          debugLog,
          verbose,
          ld64Path
        )(project)
      }

      def riddl(forJS: Boolean, version: String)(project:Project):Project = {
        project.configure(helpers.Riddl.configure(forJS, version))
      }

      def plugin(project: Project): Project = {
        project
          .configure(scala2)
          .settings(
            scalaVersion := "2.12.19",
            sbtPlugin := true
          )
      }
    }
  }

  override def projectSettings: Seq[Setting[_]] = Nil

}
