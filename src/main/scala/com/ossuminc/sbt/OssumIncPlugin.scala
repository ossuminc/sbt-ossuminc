package com.ossuminc.sbt

import sbt._
import sbt.Keys._

import com.ossuminc.sbt.OssumIncKeys._
import com.ossuminc.sbt.helpers._

object OssumIncPlugin extends AutoPlugin {

  /** The list of helper objects in this package */
  private val autoPluginHelpers: Seq[AutoPluginHelper] = {
    Seq(
      helpers.BuildInfo,
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
      helpers.SiteHelper,
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
    import OssumIncKeys._

    object With {

      def basic(projectStartYr: Int, developers: Seq[(String, String)])(project: Project): Project = {
        project.configure(
          HandyAliases.configure,
          ProjectInfo.configure,
          Git.configure,
          Header.configure(projectStartYr)
        )
      }

      def typical(projectStartYr: Int, developers: Seq[(String, String)])(project: Project): Project = {
        project.configure(
          HandyAliases.configure,
          ProjectInfo.configure(projectStartYr, developers),
          Git.configure,
          Header.configure(projectStartYr),
          Java.configure,
          Packaging.universalServer,
          Publishing.publishToSonaType,
//          Release.process,
          Resolvers,
          Scala3.apply,
          Scalafmt.apply,
          Unidoc.apply
        )
      }
    }
  }

  def identificationSettings: Seq[Setting[_]] = {
    Seq(
    )
  }

  override def projectSettings: Seq[Def.Setting[_]] = {
    Defaults.coreDefaultSettings ++ autoPluginHelpers.flatMap(_.projectSettings)
  }

  override def buildSettings: Seq[Def.Setting[_]] = {
    super.buildSettings ++ identificationSettings ++
      autoPluginHelpers.flatMap(_.buildSettings)
  }

  override def globalSettings: Seq[Def.Setting[_]] = {
    super.globalSettings ++ autoPluginHelpers.flatMap(_.globalSettings)
  }
}
