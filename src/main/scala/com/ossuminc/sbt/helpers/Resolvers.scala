package com.ossuminc.sbt.helpers

import sbt.Keys.*
import sbt.*
import sbtghpackages.GitHubPackagesPlugin.autoImport.GHPackagesResolverSyntax

object Resolvers extends AutoPluginHelper {

  private val standardResolvers: Seq[Resolver] = Seq[Resolver](
    Resolver.mavenLocal,
    Resolver.bintrayRepo("typesafe", "ivy-releases"),
    Resolver.jcenterRepo
  ) ++ Resolver.sonatypeOssRepos("releases") ++
    Resolver.sonatypeOssRepos("snapshots")

  private val sbtResolvers: Seq[Resolver] = standardResolvers :+ Resolver.bintrayRepo("sbt", "sbt-plugin-releases")

  private val scalaResolvers: Seq[Resolver] = sbtResolvers :+ Resolver.typesafeIvyRepo("releases")

  def configure(resolvers: Seq[Resolver])(project: Project): Project = {
    project.settings(externalResolvers ++= resolvers)
  }

  def configure(project: Project): Project =
    configure(Resolver.githubPackages("ossuminc") +: scalaResolvers)(project)
}
