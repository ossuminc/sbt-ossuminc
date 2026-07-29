package com.ossuminc.sbt.helpers

import com.github.sbt.git.SbtGit.useReadableConsoleGit
import sbt.*
import sbt.Keys.*
import sbtdynver.DynVerPlugin
import sbtdynver.DynVerPlugin.autoImport.{dynverSeparator, dynverVTagPrefix}

import java.io.File
import scala.util.Try

object DynamicVersioning extends AutoPluginHelper {

  object Keys {
    val versionFreshness = taskKey[Option[String]](
      "None if the resolved version matches git HEAD (or staleness cannot be proven); " +
        "Some(explanation) when the version demonstrably names a different commit"
    )
    val requireFreshVersion = taskKey[Unit](
      "Log version + HEAD SHA and FAIL if the resolved version names a different commit"
    )
    val reportVersionFreshness = taskKey[Unit](
      "Log version + HEAD SHA and WARN if the resolved version names a different commit"
    )
  }

  /** Short SHA of `HEAD`, or None outside a git repository. */
  private def headSha(base: File): Option[String] =
    Try(
      sys.process
        .Process(Seq("git", "rev-parse", "--short=8", "HEAD"), base)
        .!!(sys.process.ProcessLogger(_ => (), _ => ()))
        .trim
    ).toOption.filter(_.nonEmpty)

  /** Decide whether `ver` demonstrably names a commit other than `HEAD`.
    *
    * sbt 2's persistent server resolves `version` once at project load and holds it. A server left
    * running across commits therefore publishes artifacts stamped with an OLD commit, and reports
    * `[success]` while doing it. riddl published `1.31.0-182-2bc257ae` — two commits stale — this
    * way; `reload` produced the correct `1.31.0-184-25c4b08c`.
    *
    * Deliberately conservative: this reports staleness ONLY on positive evidence, i.e. when the
    * version carries a `-<n>-<sha>` component and that sha is not HEAD's. A version sitting exactly
    * on a tag carries no sha to compare, so it is never reported — which keeps a tagged CI release
    * (the `release.yml` publish path) from being failed by a check it cannot evaluate.
    */
  def stalenessOf(ver: String, head: String): Option[String] = {
    // dynver renders `<tag>-<commits>-<sha8>` (dynverSeparator = "-"), optionally
    // followed by a `-YYYYMMDD-HHMM` dirty stamp.
    val shaPattern = raw".*-\d+-([0-9a-f]{8}).*".r
    ver match {
      case shaPattern(sha) if sha != head =>
        Some(
          s"version '$ver' names commit $sha but HEAD is $head. The sbt server resolved " +
            "this version at project load and has held it since. Run `reload` (or " +
            "`sbt shutdown`) and retry -- otherwise the published artifact's version " +
            "claims a commit that is not the one on disk."
        )
      case _ => None
    }
  }

  /** Settings wiring the freshness check into the publish tasks. Local publishing warns (cheap to
    * redo); remote publishing fails, because GitHub Packages forbids overwriting a coordinate, so a
    * stale remote publish is not correctable in place.
    */
  private def freshnessSettings: Seq[Setting[?]] = Seq(
    Keys.versionFreshness := {
      headSha(baseDirectory.value).flatMap(stalenessOf(version.value, _))
    },
    Keys.reportVersionFreshness := {
      val log = streams.value.log
      val ver = version.value
      val head = headSha(baseDirectory.value)
      log.info(s"Publishing version $ver (HEAD ${head.getOrElse("unknown")})")
      Keys.versionFreshness.value.foreach(log.warn(_))
    },
    Keys.requireFreshVersion := {
      val log = streams.value.log
      val ver = version.value
      val head = headSha(baseDirectory.value)
      log.info(s"Publishing version $ver (HEAD ${head.getOrElse("unknown")})")
      Keys.versionFreshness.value.foreach(sys.error(_))
    },
    publish := publish.dependsOn(Keys.requireFreshVersion).value,
    publishLocal := publishLocal.dependsOn(Keys.reportVersionFreshness).value,
    publishM2 := publishM2.dependsOn(Keys.reportVersionFreshness).value
  )

  /** Check if we're in a git repository by looking for .git directory or file */
  private def isGitRepo: Boolean = {
    def checkDir(dir: File): Boolean = {
      if (dir == null) false
      else {
        val gitDir = new File(dir, ".git")
        if (gitDir.exists()) true
        else checkDir(dir.getParentFile)
      }
    }
    checkDir(new File(System.getProperty("user.dir")))
  }

  /** The configuration function to call for this plugin helper
    *
    * @param project
    *   The project to which the configuration should be applied
    *
    * @return
    *   The same project passed as an argument, post configuration
    */
  def apply(project: Project): Project = {
    val baseProject = project
      .enablePlugins(DynVerPlugin)
      .settings(
        // NEVER  SET  THIS:
        // version := "0.1"
        // IT IS HANDLED BY: sbt-dynver

        // don't prefix the version with a v
        ThisBuild / dynverVTagPrefix := false,

        // use the minus character to separate version fields
        ThisBuild / dynverSeparator := "-"
      )
      .settings(freshnessSettings*)

    // Only use native git if we're actually in a git repository.
    // This allows scripted tests to run in non-git directories while
    // still supporting git worktrees in actual development environments.
    if (isGitRepo) {
      baseProject.settings(
        // Use native git instead of JGit to support git worktrees.
        // JGit doesn't properly handle worktrees (sees .git file as bare repo).
        // Native git fully supports worktrees and is required for parallel
        // development with multiple Claude workers in separate worktrees.
        useReadableConsoleGit
      )
    } else {
      baseProject
    }
  }
}
