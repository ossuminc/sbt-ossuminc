package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*

import java.net.URI

object Scala3 extends AutoPluginHelper {

  lazy val scala_3_options: Seq[String] =
    Seq(
      "-deprecation",
      "-feature",
      "-new-syntax",
      "-Werror",
      "-pagewidth:120"
    )

  def scala_3_doc_options(
    scalaVer: String,
    projectName: Option[String] = None,
    docSiteRoot: Option[String] = None,
    docBaseURL: Option[String] = None
  ): Seq[String] = {
    Seq(
      "-deprecation",
      "-feature",
      "-groups",
      "-comment-syntax:wiki",
      s"-project-version:$scalaVer",
      "-author"
    ) ++
      projectName.map(n => s"-project:$n").toSeq ++
      docSiteRoot.map(r => s"-siteroot:$r").toSeq ++
      docBaseURL.map(u => s"-doc-canonical-base-url:$u").toSeq
  }

  /** Default apply - enables direct use as `With.Scala3` */
  def apply(project: Project): Project = configure()(project)

  /** Configure Scala 3 compilation with optional documentation settings.
    *
    * @param version
    *   Scala version (default: 3.3.7 LTS)
    * @param scala3Options
    *   Additional compiler options
    * @param projectName
    *   Project name for scaladoc (e.g., "MyProject")
    * @param docSiteRoot
    *   Root directory for documentation site
    * @param docBaseURL
    *   Base URL for API documentation
    */
  def configure(
    version: Option[String] = None,
    scala3Options: Seq[String] = Seq.empty,
    projectName: Option[String] = None,
    docSiteRoot: Option[String] = None,
    docBaseURL: Option[String] = None
  )(
    project: Project
  ): Project = {
    val apiURLSetting = docBaseURL match {
      case Some(url) => Seq(apiURL := Some(URI.create(url)))
      case None      => Seq.empty
    }

    project
      .settings(
        scalaVersion := version.getOrElse("3.3.7"),
        scalacOptions ++= scala_3_options ++ scala3Options,
        Compile / doc / scalacOptions := scala_3_doc_options(
          (Compile / scalaVersion).value,
          projectName,
          docSiteRoot,
          docBaseURL
        ),
        autoAPIMappings := true
      )
      .settings(apiURLSetting)
  }
}
