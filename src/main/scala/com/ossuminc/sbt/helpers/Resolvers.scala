package com.ossuminc.sbt.helpers

import sbt.Keys._
import sbt.{Resolver, _}
import com.ossuminc.sbt.OssumIncKeys.privateNexusResolver

object Resolvers extends AutoPluginHelper {

  override def projectSettings: Seq[Setting[_]] = {
    privateNexusResolver := None
  }

  private final val releases = "releases"

  val jgitRepo =
    "eclipse-jgit".at("http://download.eclipse.org/jgit/maven")

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
