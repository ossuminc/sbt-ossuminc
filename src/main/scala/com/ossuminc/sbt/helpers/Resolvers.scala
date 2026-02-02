package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*

/** Resolver configuration helper.
  *
  * Note: In sbt 2.x, sbt-github-packages is not yet available, so GitHub Package Registry
  * configuration must be done manually. JCenter is also deprecated/removed.
  */
object Resolvers extends AutoPluginHelper {

  private val standardResolvers: Seq[Resolver] = Seq[Resolver](
    Resolver.mavenLocal
  )

  private val sbtResolvers: Seq[Resolver] = standardResolvers

  private val scalaResolvers: Seq[Resolver] = sbtResolvers :+ Resolver.typesafeIvyRepo("releases")

  def configure(theResolvers: Seq[Resolver])(project: Project): Project = {
    project.settings(resolvers ++= theResolvers)
  }

  /** Add GitHub Packages resolver for the given organization.
    * Note: This requires GITHUB_TOKEN environment variable to be set for authentication.
    */
  def githubPackages(org: String): MavenRepository = {
    s"GitHub Package Registry ($org)".at(s"https://maven.pkg.github.com/$org/*")
  }

  /** Get GitHub token from system property (scripted tests) or environment variable.
    * Returns None if neither is set.
    */
  private def githubToken: Option[String] = {
    sys.props.get("github.token").orElse(sys.env.get("GITHUB_TOKEN"))
  }

  /** GitHub Packages credentials using token from system property or environment variable. */
  private def githubCredentials: Seq[Credentials] = {
    githubToken.map { token =>
      Credentials("GitHub Package Registry", "maven.pkg.github.com", "_", token)
    }.toSeq
  }

  def apply(project: Project): Project =
    project.settings(
      resolvers += githubPackages(RootProjectInfo.Keys.gitHubOrganization.value),
      resolvers ++= scalaResolvers,
      credentials ++= githubCredentials
    )
}
