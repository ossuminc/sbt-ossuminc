package com.ossuminc.sbt.helpers

import sbt._
import sbt.Keys._

object Scala3 extends AutoPluginHelper {

  lazy val scala_3_options: Seq[String] =
    Seq(
      "-deprecation",
      "-feature",
      "-new-syntax",
      // "-explain",
      // "-explain-types",
      "-Werror",
      "-pagewidth", "120"
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

  def apply(project: Project): Project = {
    project
      .settings(
        scalaVersion := "3.3.1",
        scalacOptions := scala_3_options,
        Compile / doc / scalacOptions := scala_3_doc_options((compile / scalaVersion).value),
        apiURL := Some(url("https://riddl.tech/apidoc/")),
        autoAPIMappings := true
      )
    project
  }

}
