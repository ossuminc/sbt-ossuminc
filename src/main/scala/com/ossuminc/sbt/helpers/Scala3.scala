package com.ossuminc.sbt.helpers

import sbt._
import sbt.Keys._

object Scala3 extends AutoPluginHelper {

  lazy val scala_3_options: Seq[String] =
    Seq(
      "-deprecation",
      "-feature",
      "-new-syntax",
      "-Werror",
      "-pagewidth:120"
    )

  def scala_3_doc_options(version: String): Seq[String] = {
    Seq(
      "-deprecation",
      "-feature",
      "-groups",
      "-project:RIDDL",
      "-comment-syntax:wiki",
      s"-project-version:$version",
      "-siteroot:doc/src/hugo/static/apidoc",
      "-author",
      "-doc-canonical-base-url:https://riddl.tech/apidoc"
    )
  }

  def configure(project: Project): Project = Scala3.configure(
    Option.empty[String],
    Seq.empty[String]
  )(project)

  def configure(
    version: Option[String] = Option.empty[String],
    scala3Options: Seq[String] = Seq.empty[String]
  )(
    project: Project
  ): Project = {
    project
      .settings(
        scalaVersion := version.getOrElse("3.3.7"),
        scalacOptions ++= scala_3_options ++ scala3Options,
        Compile / doc / scalacOptions := scala_3_doc_options((compile / scalaVersion).value),
        apiURL := Some(url("https://riddl.tech/apidoc/")),
        autoAPIMappings := true
      )
  }
}
