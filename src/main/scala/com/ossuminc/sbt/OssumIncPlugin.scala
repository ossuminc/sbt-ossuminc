package com.ossuminc.sbt

import sbt.*
import sbt.Keys.*
import sbt.librarymanagement.Resolver

object OssumIncPlugin extends AutoPlugin {

  object autoImport {

    object Root {

      /** Define a Root level project whether it is for a single-project repo or a unirepo with many sub-projects. This
        * project is configured with a shell prompt, and the standard project information at ThisBuild scope
        * @param id
        *   The artifact id name for the root project.
        */
      def apply(
        modName: String = "",
        projName: String = "root",
        org: String = "com.ossuminc",
        orgName: String = "Ossum, Inc.",
        orgPage: URL = url("https://com.ossuminc/"),
        startYr: Int = 2023,
        devs: List[Developer] = List.empty
      ): Project = {
        val result = Project
          .apply("root", file(System.getProperty("user.dir")))
          .configure(
            helpers.RootProjectInfo.initialize(
              modName,
              startYr,
              org,
              orgName,
              orgPage,
              devs
            )
          )
          .settings(name := projName)
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
        * @param id
        *   The name of the artifact that will be produced
        * @param dirName
        *   The name of the sub-directory in which the module is located.
        * @return
        *   The project that was created and configure.
        */
      def apply(dirName: String, modName: String = ""): Project = {
        Project
          .apply(dirName, file(dirName))
          .settings(
            name := dirName,
            moduleName := { if (modName.isEmpty) dirName else modName }
          )
      }
    }

    object Plugin {
      def apply(dirName: String, modName: String = ""): Project = {
        Project
          .apply(dirName, file(dirName))
          .configure(helpers.Plugin.configure)
          .settings(
            name := dirName,
            moduleName := {
              if (modName.isEmpty) dirName else modName
            }
          )
      }
    }

    private type ConfigFunc = Project => Project

    object With {
      val aliases: ConfigFunc = helpers.HandyAliases.configure
      val build_info: ConfigFunc = helpers.BuildInfo.configure
      val dynver: ConfigFunc = helpers.DynamicVersioning.configure
      val git: ConfigFunc = helpers.Git.configure
      val header: ConfigFunc = helpers.Header.configure
      val java: ConfigFunc = helpers.Java.configure
      val misc: ConfigFunc = helpers.Miscellaneous.configure
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
          publishTo := Some(Resolver.defaultLocal)
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

      def plugin(project: Project): Project = {
        project
          .configure(scala2)
          .settings(
            scalaVersion := "2.12.18"
          )
      }
    }
  }

  override def projectSettings: Seq[Setting[_]] = Nil

}
