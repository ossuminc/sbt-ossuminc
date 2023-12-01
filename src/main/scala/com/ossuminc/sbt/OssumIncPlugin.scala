package com.ossuminc.sbt

import com.ossuminc.sbt.helpers.AutoPluginHelper
import com.ossuminc.sbt.helpers.Miscellaneous.buildShellPrompt
import com.ossuminc.sbt.helpers.ProjectInfo.Keys.projectStartYear
import sbt.{url, *}
import sbt.Keys.*
import sbt.librarymanagement.Resolver

object OssumIncPlugin extends AutoPlugin {

  /** Capture the AutoPluginHelpers configured in autoImport.With */
  private var helpersToRequire: Seq[helpers.AutoPluginHelper] = Seq.empty

  override def requires: Plugins = {
    helpersToRequire.foldLeft(empty) { (b, helper) =>
      helper.autoPlugins.foldLeft(b) { (b, plugin) =>
        b && plugin
      }
    }
  }

  object autoImport {

    object Root {

      /** Define a Root level project whether it is for a single-project repo or a unirepo with many sub-projects. This
        * project is configured with a shell prompt, and the standard project information at ThisBuild scope
        * @param id
        *   The artifact id name for the root project.
        */
      def apply(
        id: String,
        org: String = "com.ossuminc",
        orgName: String = "Ossum, Inc.",
        orgPage: URL = url("https://com.ossuminc/"),
        startYr: Int = 2023,
        devs: List[Developer] = List.empty
      ): Project = {
        Project
          .apply(id, file("."))
          .configure(helpers.ProjectInfo.configure)
          .configure(helpers.Scalafmt.configure)
          .settings(
            name := id,
            projectStartYear := startYr,
            organization := org,
            organizationName := orgName,
            organizationHomepage := Some(orgPage),
            developers := devs,
            Global / shellPrompt := buildShellPrompt.value
          )
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
      def apply(id: String, dirName: String): Project = {
        Project
          .apply(id, file(dirName))
          .settings(
            name := id
          )
      }
    }

    object With {
      val aliases: helpers.HandyAliases.type = helpers.HandyAliases
      val build_info: helpers.BuildInfo.type = helpers.BuildInfo
      val dynver: helpers.DynamicVersioning.type = helpers.DynamicVersioning
      val git: helpers.Git.type = helpers.Git
      val header: helpers.Header.type = helpers.Header
      val java: helpers.Java.type = helpers.Java
      val misc: helpers.Miscellaneous.type = helpers.Miscellaneous
      val project_info: helpers.ProjectInfo.type = helpers.ProjectInfo
      val publishing: helpers.Publishing.type = helpers.Publishing
      val release: helpers.Release.type = helpers.Release
      val resolvers: helpers.Resolvers.type = helpers.Resolvers
      val scala2: helpers.Scala2.type = helpers.Scala2
      val scala3: helpers.Scala3.type = helpers.Scala3
      val scalafmt: helpers.Scalafmt.type = helpers.Scalafmt
      val unidoc: helpers.Unidoc.type = helpers.Unidoc

      def noPublishing(project: Project): Project = {
        project.settings(
          publishArtifact := false, // no artifact to publish for the virtual root project
          publish := {}, // just to be sure
          publishLocal := {}, // and paranoid
          publishTo := Some(Resolver.defaultLocal)
        )
      }

      def these(helpers: AutoPluginHelper*)(project: Project): Project = {
        val newHelpers = helpers.foldLeft(Seq.empty[AutoPluginHelper])((s, h) => (s :+ h) ++ h.usedHelpers)
        helpersToRequire = (helpersToRequire ++ newHelpers).distinct
        newHelpers.foldLeft(project) { (p, helper) =>
          p.configure(helper.configure)
        }
      }

      def basic(project: Project): Project = {
        these(aliases, build_info, dynver, git, header)(project)
      }

      def typical(project: Project): Project = {
        project.configure(basic)
        these(publishing, release, scala3, unidoc)(project)
      }

      def everything(project: Project): Project = {
        project.configure(typical)
        these(java)(project)
      }
      def plugin(project: Project): Project = {
        project
          .configure(scala2.configure)
          .settings(
            scalaVersion := "2.12.18"
          )

      }
    }
  }

  override def projectSettings: Seq[Setting[_]] = Nil

}
