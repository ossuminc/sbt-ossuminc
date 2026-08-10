package com.ossuminc.sbt.helpers

import sbt._
import sbt.Keys._

/** Helper for publishing npm packages to registries.
  *
  * Provides tasks for publishing to npmjs.com and GitHub Packages. Authentication is via
  * environment variables:
  *   - `NPM_TOKEN` for npmjs.com
  *   - `GITHUB_TOKEN` for GitHub Packages
  *
  * Usage:
  * {{{
  * .jsConfigure(
  *   With.Publishing.npm(
  *     registries = Seq("npmjs", "github")
  *   )
  * )
  * }}}
  *
  * Depends on [[NpmPackaging]] — the project must also be configured with `With.Packaging.npm(...)`
  * to provide `npmPrepare` and `npmPack` tasks.
  */
object NpmPublishing {

  object Keys {
    val npmPublish = taskKey[Unit](
      "Publish npm package to all configured registries"
    )
    val npmPublishNpmjs = taskKey[Unit](
      "Publish npm package to npmjs.com"
    )
    val npmPublishGithub = taskKey[Unit](
      "Publish npm package to GitHub Packages"
    )
    val npmRegistries = settingKey[Seq[String]](
      "Target npm registries (\"npmjs\", \"github\")"
    )
    val npmDistTag = settingKey[String](
      "npm dist-tag to publish under. Derived from the version by default; " +
        "set explicitly to override the heuristic"
    )
  }

  /** The npm dist-tag implied by a version.
    *
    * `npm publish` moves the `latest` tag BY DEFAULT, whatever the version string says. Publishing
    * `1.32.0-rc.1` without `--tag` therefore makes a release candidate what a plain `npm install
    * <pkg>` resolves to. One rule covers both prerelease shapes:
    *
    *   - `1.32.0-rc.1` (a release candidate) -> `rc`
    *   - `1.32.0-4-abc12345` (dynver between tags, or any other prerelease) -> `dev`
    *   - `1.32.0` -> `latest`
    */
  def distTagFor(ver: String): String =
    if (DynamicVersioning.isReleaseCandidateVersion(ver)) "rc"
    else if (DynamicVersioning.isPrereleaseVersion(ver)) "dev"
    else "latest"

  /** Publish to npmjs.com from a prepared package directory.
    *
    * @param prepDir
    *   the directory containing the assembled npm package
    * @param log
    *   sbt logger
    */
  /** The version-derived `npmDistTag` default.
    *
    * Lives in [[com.ossuminc.sbt.OssumIncPlugin.projectSettings]] so a project override wins
    * wherever it is written, rather than only after the `.jsConfigure(With.Publishing.npm(...))`
    * call — this is the escape hatch for cases the heuristic gets wrong.
    */
  def defaultSettings: Seq[Setting[?]] = Seq(
    Keys.npmDistTag := distTagFor(version.value)
  )

  /** `npm publish` arguments, adding `--tag` for anything that must not become `latest`. */
  private def publishCmd(distTag: String): Seq[String] = {
    val base = Seq("npm", "publish", "--access", "public")
    if (distTag == "latest") base else base ++ Seq("--tag", distTag)
  }

  private def publishToNpmjs(prepDir: File, distTag: String, log: Logger): Unit = {
    val token = sys.env.getOrElse(
      "NPM_TOKEN",
      sys.error(
        "NPM_TOKEN environment variable is not set. " +
          "Set it to your npmjs.com access token."
      )
    )

    log.info("Publishing to npmjs.com...")

    // Write .npmrc for auth in the package directory
    val npmrc = prepDir / ".npmrc"
    IO.write(
      npmrc,
      "//registry.npmjs.org/:_authToken=${NPM_TOKEN}\n" +
        "registry=https://registry.npmjs.org/\n"
    )

    val cmd = publishCmd(distTag)
    log.info(s"Publishing under dist-tag '$distTag'")
    log.info(s"Running: ${cmd.mkString(" ")}")

    val env = Seq("NPM_TOKEN" -> token)
    val exitCode = sys.process.Process(cmd, prepDir, env*).!

    // Clean up .npmrc regardless of outcome
    IO.delete(npmrc)

    if (exitCode != 0) {
      sys.error(
        s"npm publish to npmjs.com failed with exit code $exitCode"
      )
    }
    log.info("Successfully published to npmjs.com")
  }

  /** Publish to GitHub Packages from a prepared package directory.
    *
    * @param prepDir
    *   the directory containing the assembled npm package
    * @param scope
    *   the npm scope (e.g. "@ossuminc")
    * @param log
    *   sbt logger
    */
  private def publishToGithub(
    prepDir: File,
    scope: String,
    distTag: String,
    log: Logger
  ): Unit = {
    val token = sys.env.getOrElse(
      "GITHUB_TOKEN",
      sys.error(
        "GITHUB_TOKEN environment variable is not set. " +
          "Set it to a GitHub token with write:packages scope."
      )
    )
    if (scope.isEmpty) {
      sys.error(
        "npm scope is required for GitHub Packages publishing. " +
          "Set scope in With.Packaging.npm(scope = \"@yourorg\", ...)"
      )
    }

    val registryOrg = scope.stripPrefix("@")
    val registryUrl = s"https://npm.pkg.github.com/$registryOrg"

    log.info(s"Publishing to GitHub Packages ($registryUrl)...")

    // Write .npmrc for auth in the package directory
    val npmrc = prepDir / ".npmrc"
    IO.write(
      npmrc,
      s"//npm.pkg.github.com/:_authToken=$${GITHUB_TOKEN}\n" +
        s"${scope}:registry=$registryUrl\n"
    )

    val cmd = publishCmd(distTag)
    log.info(s"Publishing under dist-tag '$distTag'")
    log.info(s"Running: ${cmd.mkString(" ")}")

    val env = Seq("GITHUB_TOKEN" -> token)
    val exitCode = sys.process.Process(cmd, prepDir, env*).!

    // Clean up .npmrc regardless of outcome
    IO.delete(npmrc)

    if (exitCode != 0) {
      sys.error(
        "npm publish to GitHub Packages failed with " +
          s"exit code $exitCode"
      )
    }
    log.info("Successfully published to GitHub Packages")
  }

  /** Configure npm publishing to one or more registries.
    *
    * @param registries
    *   Target registries. Supported values:
    *   - `"npmjs"` — registry.npmjs.org (uses `NPM_TOKEN` env var)
    *   - `"github"` — npm.pkg.github.com (uses `GITHUB_TOKEN` env var)
    */
  def npm(
    registries: Seq[String] = Seq("npmjs")
  )(project: Project): Project = {
    project.settings(
      Keys.npmRegistries := registries,
      Keys.npmPublishNpmjs := {
        val log = streams.value.log
        val prepDir = NpmPackaging.Keys.npmPrepare.value
        publishToNpmjs(prepDir, Keys.npmDistTag.value, log)
      },
      Keys.npmPublishGithub := {
        val log = streams.value.log
        val prepDir = NpmPackaging.Keys.npmPrepare.value
        val scope = NpmPackaging.Keys.npmScope.value
        publishToGithub(prepDir, scope, Keys.npmDistTag.value, log)
      },
      Keys.npmPublish := {
        val log = streams.value.log
        val prepDir = NpmPackaging.Keys.npmPrepare.value
        val scope = NpmPackaging.Keys.npmScope.value
        val regs = Keys.npmRegistries.value
        val distTag = Keys.npmDistTag.value

        regs.foreach {
          case "npmjs" =>
            publishToNpmjs(prepDir, distTag, log)
          case "github" =>
            publishToGithub(prepDir, scope, distTag, log)
          case other =>
            sys.error(
              s"Unknown npm registry: '$other'. " +
                "Supported: \"npmjs\", \"github\""
            )
        }
      }
    )
  }
}
