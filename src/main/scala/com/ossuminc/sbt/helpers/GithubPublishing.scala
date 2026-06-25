package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*

/** Configure publishing to GitHub Packages using plain Maven publishing.
  *
  * sbt 2.x note: the `sbt-github-packages` plugin (com.codecommit) was abandoned
  * and never published an sbt 2.0 build. GitHub Packages is just a Maven
  * repository, so we configure `publishTo`, a resolver, and `Credentials`
  * directly instead of depending on the plugin.
  */
object GithubPublishing extends AutoPluginHelper {

  def apply(project: Project): Project = {
    project.settings(
      publishMavenStyle := true,
      publishTo := {
        val org = RootProjectInfo.requireConfigured(
          RootProjectInfo.Keys.gitHubOrganization.value, "gitHubOrganization", "GithubPublishing"
        )
        val repo = RootProjectInfo.requireConfigured(
          RootProjectInfo.Keys.gitHubRepository.value, "gitHubRepository", "GithubPublishing"
        )
        Some("GitHub Packages" at s"https://maven.pkg.github.com/$org/$repo")
      },
      resolvers += {
        val org = RootProjectInfo.requireConfigured(
          RootProjectInfo.Keys.gitHubOrganization.value, "gitHubOrganization", "GithubPublishing"
        )
        "GitHub Packages" at s"https://maven.pkg.github.com/$org/_"
      },
      credentials ++= githubCredentials
    )
  }

  /** Credentials for `maven.pkg.github.com` from the `github.token` system
    * property (set for scripted tests via scriptedLaunchOpts) or the
    * `GITHUB_TOKEN` env var. The user comes from `GITHUB_ACTOR` (set in CI);
    * GitHub accepts the token regardless of the supplied username.
    */
  private[sbt] def githubCredentials: Seq[Credentials] = {
    val token = sys.props.get("github.token").orElse(sys.env.get("GITHUB_TOKEN"))
    val user = sys.env.getOrElse("GITHUB_ACTOR", "_")
    token.map { t =>
      Credentials("GitHub Package Registry", "maven.pkg.github.com", user, t)
    }.toSeq
  }
}
