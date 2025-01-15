package com.ossuminc.sbt.helpers

import com.lightbend.paradox.sbt.ParadoxPlugin
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport.*
import com.ossuminc.sbt.helpers.RootProjectInfo.Keys
import sbt.*
import sbt.Keys.*

object Paradox extends AutoPluginHelper {

  override def configure(project: Project): Project = {
    configureParadox()(project)
  }

  def configureParadox(
    extrefs: Map[String, String] = Map.empty,
    tocNavDepth: Int = 3
  )(project: Project): Project = {
    project
      .enablePlugins(ParadoxPlugin)
      .settings(
        // Basic Paradox settings
        paradoxTheme := Some(builtinParadoxTheme("generic")),
        paradoxProperties ++= Map(
          // Version displayed in docs
          "project.version" -> version.value,
          "scala.version" -> scalaVersion.value,
          "scala.binary.version" -> scalaBinaryVersion.value,

          // Links to GitHub
          "github.base_url" ->
            s"https://github.com/${Keys.gitHubOrganization.value}/${Keys.gitHubRepository}",
          "snip.github_link" -> "true",

          // Akka-specific external references
          "extref.akka.base_url" -> "https://doc.akka.io/docs/akka/current/%s",
          "extref.akka.http.base_url" -> "https://doc.akka.io/docs/akka-http/current/%s",
          "extref.akka.grpc.base_url" -> "https://doc.akka.io/docs/akka-grpc/current/%s",

          // Other common references
          "extref.scala.base_url" -> "https://www.scala-lang.org/api/current/%s",
          "extref.pekko.base_url" -> "https://pekko.apache.org/docs/pekko/current/%s",

          // Canonical URL (for SEO)
          "canonical.base_url" ->
            s"https://${Keys.gitHubOrganization.value}.github.io/${Keys.gitHubRepository.value}"
        ) ++ extrefs,

        // Source directory configuration
        Compile / paradoxMarkdownToHtml / sourceDirectory := sourceDirectory.value / "main" / "paradox",

        // Navigation depth in ToC
        paradoxNavigationDepth := tocNavDepth,

        // Optional: Group API links by package
        paradoxGroups := Map("Language" -> Seq("scala.*")),

        // Optional: Custom CSS/JS
        paradoxOverlayDirectories := Seq(
          baseDirectory.value / "src" / "main" / "paradox" / "_template"
        )
      )

  }
}
