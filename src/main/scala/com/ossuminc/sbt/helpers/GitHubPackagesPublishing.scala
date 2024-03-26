package com.ossuminc.sbt.helpers
import sbt.*
import sbt.Keys.*
import sbtghpackages.GitHubPackagesKeys.{githubOwner, githubPublishTo, githubRepository, githubTokenSource}
import sbtghpackages.{GitHubPackagesPlugin, TokenSource}
import sbtghpackages.GitHubPackagesPlugin.autoImport.GHPackagesResolverSyntax

object GitHubPackagesPublishing extends AutoPluginHelper {

  val githubPackagesResolver: Resolver =
    Resolver.url("GitHub Packages", url("https://maven.pkg.github.com/"))(
      Patterns("[organisation]/[module]/[revision]/[artifact].[ext]")
    )

  /** The configuration function to call for this plugin helper
    *
    * @param project
    *   The project to which the configuration should be applied
    *
    * @return
    *   The same project passed as an argument, post configuration
    */
  def configure(project: Project): Project = {
    project
      .enablePlugins(GitHubPackagesPlugin)
      .settings(
        githubOwner := RootProjectInfo.Keys.gitHubOrganization.value,
        githubRepository := RootProjectInfo.Keys.gitHubRepository.value,
        githubTokenSource := TokenSource.Environment("GITHUB_TOKEN"),
        resolvers += Resolver.githubPackages(RootProjectInfo.Keys.gitHubOrganization.value),
        publishMavenStyle := true,
        publishTo := githubPublishTo.value
      )
  }

}