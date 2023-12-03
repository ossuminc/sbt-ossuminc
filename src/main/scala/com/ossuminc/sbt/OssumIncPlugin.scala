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
        id: String,
        org: String = "com.ossuminc",
        orgName: String = "Ossum, Inc.",
        orgPage: URL = url("https://com.ossuminc/"),
        startYr: Int = 2023,
        devs: List[Developer] = List.empty
      ): Project = {
        Project
          .apply(id, file("."))
          .configure(
            helpers.RootProjectInfo.initialize(
              id,
              org,
              orgName,
              orgPage,
              startYr,
              devs
            )
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
            name := id,
            moduleName := id
          )
      }
    }

    type ConfigFunc = (Project) => Project

    object With {
      def aliases(project: Project): Project = project.configure(helpers.HandyAliases.configure)
      def build_info(project: Project): Project = project.configure(helpers.BuildInfo.configure)
      def dynver(project: Project): Project = project.configure(helpers.DynamicVersioning.configure)
      def git(project: Project): Project = project.configure(helpers.Git.configure)
      def header(project: Project): Project = project.configure(helpers.Header.configure)
      def java(project: Project): Project = project.configure(helpers.Java.configure)
      def misc(project: Project): Project = project.configure(helpers.Miscellaneous.configure)
      def publishing(project: Project): Project = project.configure(helpers.Publishing.configure)
      def release(project: Project): Project = project.configure(helpers.Release.configure)
      def resolvers(project: Project): Project = project.configure(helpers.Resolvers.configure)
      def scala2(project: Project): Project = project.configure(helpers.Scala2.configure)
      def scala3(project: Project): Project = project.configure(helpers.Scala3.configure)
      def scalafmt(project: Project): Project = project.configure(helpers.Scalafmt.configure)
      def unidoc(project: Project): Project = project.configure(helpers.Unidoc.configure)

      def noPublishing(project: Project): Project = {
        project.settings(
          publishArtifact := false, // no artifact to publish for the virtual root project
          publish := {}, // just to be sure
          publishLocal := {}, // and paranoid
          publishTo := Some(Resolver.defaultLocal)
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
        these(publishing, release, scala3, unidoc)(project)
      }

      def everything(project: Project): Project = {
        project.configure(typical)
        these(java, misc, build_info)(project)
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
