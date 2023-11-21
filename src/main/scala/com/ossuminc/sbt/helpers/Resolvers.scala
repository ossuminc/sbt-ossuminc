package com.ossuminc.sbt.helpers

import com.ossuminc.sbt.helpers.Release.Keys.privateNexusResolver
import sbt.Keys.*
import sbt.*

object Resolvers extends AutoPluginHelper {

  private final val releases = "releases"

  val standardResolvers: Seq[Resolver] = Seq[Resolver](
    Resolver.bintrayRepo("typesafe", "ivy-releases"),
    Resolver.jcenterRepo,
    Resolver.file(
      "local",
      _root_.sbt.file(Path.userHome.absolutePath + "/.ivy2/local")
    )(Resolver.ivyStylePatterns),
    Resolver.mavenLocal,
    privateNexusResolver.value.getOrElse(Resolver.mavenCentral)
  ) ++ Resolver.sonatypeOssRepos(releases) ++
    Resolver.sonatypeOssRepos("snapshots")

  val sbtResolvers: Seq[Resolver] =
    Seq(Resolver.bintrayRepo("sbt", "sbt-plugin-releases")) ++ standardResolvers

  val scalaResolvers: Seq[Resolver] =
    Seq(Resolver.typesafeIvyRepo(releases)) ++ sbtResolvers

  def configure(resolvers: Seq[Resolver])(project: Project): Project = {
    project.settings(externalResolvers ++= resolvers)
  }

  def configure(project: Project): Project = {
    configure(scalaResolvers)(project)
  }
}
