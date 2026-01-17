package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*
import sbtghpackages.GitHubPackagesPlugin.autoImport.GHPackagesResolverSyntax

object Resolvers extends AutoPluginHelper {

  private val standardResolvers: Seq[Resolver] = Seq[Resolver](
    Resolver.mavenLocal,
    Resolver.jcenterRepo
  )

  private val sbtResolvers: Seq[Resolver] = standardResolvers

  private val scalaResolvers: Seq[Resolver] = sbtResolvers :+ Resolver.typesafeIvyRepo("releases")

  def configure(resolvers: Seq[Resolver])(project: Project): Project = {
    project.settings(externalResolvers ++= resolvers)
  }

  def apply(project: Project): Project =
    project.settings(
      resolvers += Resolver.githubPackages(RootProjectInfo.Keys.gitHubOrganization.value),
      resolvers ++= scalaResolvers
    )
}
