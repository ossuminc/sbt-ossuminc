package com.ossuminc.sbt

import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.graalvmnativeimage.GraalVMNativeImagePlugin
import com.typesafe.sbt.packager.universal.UniversalDeployPlugin
import sbt.*
import sbt.Keys.*
import sbt.librarymanagement.Resolver

object OssumIncPlugin extends AutoPlugin {

  object autoImport {

    object Root {

      /** Define a Root level project whether it is for a single-project repo or a unirepo with many sub-projects. This
        * project is configured with a shell prompt, and the standard project information at ThisBuild scope
        * @param modName
        *   The artifact id name for the root project.
        * @param projName
        *   The name of the sbt project
        * @param org
        *   The name of the organization responsible for this project in Java package format
        * @param orgName
        *   The legal name of the organization
        * @param orgPage
        *   The website to use for the organizaiton
        * @param startYr
        *   The year in which this project started, for copyright purposes
        * @param devs
        *   A list of Developer specifications to include in POM (required by Maven)
        * @return
        *   The project that was created and configured.
        */
      def apply(
        modName: String = "",
        projName: String = "root",
        org: String = "com.ossuminc",
        orgName: String = "Ossum, Inc.",
        orgPage: URL = url("https://com.ossuminc/"),
        maintainerEmail: String = "reid@ossuminc.com",
        startYr: Int = 2023,
        devs: List[Developer] = List.empty
      ): Project = {
        val result = Project
          .apply("root", file(System.getProperty("user.dir")))
          .enablePlugins(OssumIncPlugin)
          .configure(
            helpers.RootProjectInfo.initialize(
              modName,
              startYr,
              org,
              orgName,
              orgPage,
              maintainerEmail,
              devs
            ),
            helpers.Resolvers.configure
          )
          .settings(
            name := projName
          )
        if (modName.isEmpty) {
          result.configure(With.noPublishing)
        } else {
          result
        }
      }
    }

    object Module {

      /** Define a sub-project or module of the root project. Make sure to use the [[Root]] function before this Module
        * is defined. No configuration is applied but you can do that by using the various With.* functions in this
        * plugin. `With.typical` is typical for Scala3 development
        * @param dirName
        *   The name of the sub-directory in which the module is located.
        * @param modName
        *   The name of the artifact to be published. If blank, it will default to the dirName
        * @return
        *   The project that was created and configured.
        */
      def apply(dirName: String, modName: String = ""): Project = {
        Project
          .apply(dirName, file(dirName))
          .enablePlugins(OssumIncPlugin, JavaAppPackaging)
          .settings(
            name := dirName,
            moduleName := { if (modName.isEmpty) dirName else modName }
          )
      }
    }

    object Plugin {

      /** Define a sub-project that produces an sbt plugin. It is necessary to also have used the [[Root]] function
        * because what that function sets up is necessary for publishing this module. scoverage doesn't work with sbt
        * plugins so it is disabled.
        * @param dirName
        *   The name of the directory in which the plugin code and tests exist
        * @param modName
        *   The name of the published artifact. If blank, the `dirName` will be used
        * @return
        *   The configured sbt project that is ready to build an sbt plugin
        */
      def apply(dirName: String, modName: String = ""): Project = {
        Project
          .apply(dirName, file(dirName))
          .enablePlugins(OssumIncPlugin)
          .configure(helpers.Plugin.configure)
          .settings(
            name := dirName,
            moduleName := { if (modName.isEmpty) dirName else modName }
          )
      }
    }

    object Program {

      /** Define a sub-project that produces an executable program. It is necessary to also have used the [[Root]]
        * function because what that function sets up is necessary for publishing this module.
        * @param dirName
        *   The name of the directory in which the plugin code and tests exist
        * @param appName
        *   The name of the published artifact. If blank, the `dirName` will be used
        * @return
        *   The configured sbt project that is ready to build an sbt plugin
        */
      def apply(dirName: String, appName: String): Project = {
        Project
          .apply(dirName, file(dirName))
          .enablePlugins(OssumIncPlugin, JavaAppPackaging, UniversalDeployPlugin, GraalVMNativeImagePlugin)
          .settings(
            name := dirName,
            moduleName := { if (appName.isEmpty) dirName else appName }
          )
      }
    }

    private type ConfigFunc = Project => Project

    object With {
      val aliases: ConfigFunc = helpers.HandyAliases.configure
      val build_info: ConfigFunc = helpers.BuildInfo.configure
      val dynver: ConfigFunc = helpers.DynamicVersioning.configure
      val git: ConfigFunc = helpers.Git.configure
      val githubPackages: ConfigFunc = helpers.GitHubPackagesPublishing.configure
      val header: ConfigFunc = helpers.Header.configure
      val java: ConfigFunc = helpers.Java.configure
      val misc: ConfigFunc = helpers.Miscellaneous.configure
      val native: ConfigFunc = helpers.Native.configure
      val publishing: ConfigFunc = helpers.Publishing.configure
      val release: ConfigFunc = helpers.Release.configure
      val resolvers: ConfigFunc = helpers.Resolvers.configure
      val scala2: ConfigFunc = helpers.Scala2.configure
      val scala3: ConfigFunc = helpers.Scala3.configure
      val scalafmt: ConfigFunc = helpers.Scalafmt.configure
      val scoverage: ConfigFunc = helpers.ScalaCoverage.configure
      val unidoc: ConfigFunc = helpers.Unidoc.configure

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
        project.settings(
          helpers.ScalaCoverage.Keys.coveragePercent := percent
        )
      }

      def these(cfuncs: ConfigFunc*)(project: Project): Project = {
        cfuncs.foldLeft(project) { (p, func) =>
          p.configure(func)
        }
      }

      def basic(project: Project): Project = {
        these(aliases, dynver, git, header)(project)
      }

      def typical(project: Project): Project = {
        project.configure(basic)
        these(scala3, scoverage, publishing, unidoc)(project)
      }

      def everything(project: Project): Project = {
        project.configure(typical)
        these(java, misc, build_info, release)(project)
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

      def plugin(project: Project): Project = {
        project
          .configure(scala2)
          .settings(
            scalaVersion := "2.12.18",
            sbtPlugin := true
          )
      }
    }
  }

  override def projectSettings: Seq[Setting[_]] = Nil

}
