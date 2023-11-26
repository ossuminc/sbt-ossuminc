package com.ossuminc.sbt

import com.ossuminc.sbt.helpers.AutoPluginHelper
import sbt.*

object OssumIncPlugin extends AutoPlugin {

  /** Capture the AutoPluginHelpers configured in autoImport.With */
  private var helpersToRequire: Seq[helpers.AutoPluginHelper] = Seq(helpers.ProjectInfo)

  override def requires: Plugins = {
    helpersToRequire.foldLeft(empty) { (b, helper) =>
      helper.autoPlugins.foldLeft(b) { (b, plugin) =>
        b && plugin
      }
    }
  }

  object autoImport {

    val git: helpers.Git.type = helpers.Git
    val aliases: helpers.HandyAliases.type = helpers.HandyAliases
    val header: helpers.Header.type = helpers.Header
    val info: helpers.ProjectInfo.type = helpers.ProjectInfo
    val java: helpers.Java.type = helpers.Java
    val misc: helpers.Miscellaneous.type = helpers.Miscellaneous
    val publishing: helpers.Publishing.type = helpers.Publishing
    val release: helpers.Release.type = helpers.Release
    val resolvers: helpers.Resolvers.type = helpers.Resolvers
    val scala2: helpers.Scala2.type = helpers.Scala2
    val scala3: helpers.Scala3.type = helpers.Scala3
    val scalafmt: helpers.Scalafmt.type = helpers.Scalafmt
    val unidoc: helpers.Unidoc.type = helpers.Unidoc

    object With {

      def these(helpers: AutoPluginHelper*)(project: Project): Project = {
        helpers.foreach { helper => helpersToRequire = helpersToRequire :+ helper }
        helpersToRequire.foldLeft(project) { (p, helper) =>
          println(s"Configuring ${helper.getClass.getSimpleName}")
          p.configure(helper.configure)
        }
      }

      def basic(project: Project): Project = {
        these(git, aliases, header)(project)
      }

      def typical(project: Project): Project = {
        these(git, aliases, header, publishing, release, scala3, scalafmt, unidoc)(project)
      }

      def everything(project: Project): Project = {
        these(git, aliases, misc, header, java, publishing, resolvers, release, scala3, scalafmt, unidoc)(project)
      }
    }
  }

  override def projectSettings: Seq[Setting[_]] = Nil

}
