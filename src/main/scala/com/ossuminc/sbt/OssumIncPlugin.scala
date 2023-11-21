package com.ossuminc.sbt

import sbt._
import com.ossuminc.sbt.helpers._

object OssumIncPlugin extends AutoPlugin {

  /** The list of helper objects in this package */
  private val autoPluginHelpers: Seq[AutoPluginHelper] = {
    Seq(
      helpers.Git,
      helpers.HandyAliases,
      helpers.Header,
      helpers.Java,
      helpers.Miscellaneous,
      helpers.Packaging,
      helpers.ProjectInfo,
      helpers.Publishing,
      helpers.Release,
      helpers.Resolvers,
      helpers.Scala2,
      helpers.Scala3,
      helpers.Scalafmt,
      helpers.Unidoc
    )
  }

  /** Extract the AutoPlugins needed from the Helpers */
  private val autoPlugins: Seq[AutoPlugin] = {
    autoPluginHelpers.flatMap(_.autoPlugins).distinct
  }

  override def requires: Plugins = {
    autoPlugins.foldLeft(empty) { (b, plugin) =>
      b && plugin
    }
  }

  object autoImport {

    object With {

      def basic(project: Project): Project = {
        project.configure(

          ProjectInfo.configure,
          Git.configure,
          HandyAliases.configure,
          Header.configure
        )
      }

      def typical(project: Project): Project = {
        project.configure(basic)
        project.configure(
          Java.configure,
          Packaging.universalServer,
          Publishing.publishToSonaType,
          helpers.Resolvers.configure,
          Release.configure,
          Scala3.configure,
          Scalafmt.configure,
          Unidoc.configure
        )
      }
    }
  }
}
