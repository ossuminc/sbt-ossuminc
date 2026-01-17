package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*
import sbtghpackages.GitHubPackagesPlugin.autoImport.*

object GithubPublishing extends AutoPluginHelper {

  def apply(project: Project): Project = {
    project
      .enablePlugins(sbtghpackages.GitHubPackagesPlugin)
      .settings(
        githubOwner := RootProjectInfo.requireConfigured(
          RootProjectInfo.Keys.gitHubOrganization.value,
          "gitHubOrganization",
          "GithubPublishing"
        ),
        githubRepository := RootProjectInfo.requireConfigured(
          RootProjectInfo.Keys.gitHubRepository.value,
          "gitHubRepository",
          "GithubPublishing"
        ),
        githubTokenSource := TokenSource.Or(
          TokenSource.GitConfig("github.token"),
          TokenSource.Environment("GITHUB_TOKEN")
        ),
        publishMavenStyle := true,
        resolvers += Resolver.githubPackages(
          RootProjectInfo.requireConfigured(
            RootProjectInfo.Keys.gitHubOrganization.value,
            "gitHubOrganization",
            "GithubPublishing"
          )
        ),
        publishTo := githubPublishTo.value
      )
  }
}
