package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*
import sbtghpackages.GitHubPackagesPlugin.autoImport.*

object GithubPublishing extends AutoPluginHelper {

  def configure(project: Project): Project = {
    project
      .enablePlugins(sbtghpackages.GitHubPackagesPlugin)
      .settings(
        githubOwner := RootProjectInfo.Keys.gitHubOrganization.value,
        githubRepository := RootProjectInfo.Keys.gitHubRepository.value,
        githubTokenSource := TokenSource.GitConfig("github.token") || TokenSource.Environment("GITHUB_TOKEN"),
        publishMavenStyle := true,
        resolvers += Resolver.githubPackages(RootProjectInfo.Keys.gitHubOrganization.value),
        publishTo := githubPublishTo.value
      )
  }
}
