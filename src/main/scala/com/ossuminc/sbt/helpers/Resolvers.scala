package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*

object Resolvers extends AutoPluginHelper {

  private val standardResolvers: Seq[Resolver] = Seq[Resolver](
    Resolver.mavenLocal
  )

  private val scalaResolvers: Seq[Resolver] =
    standardResolvers :+ Resolver.typesafeIvyRepo("releases")

  def configure(resolvers: Seq[Resolver])(project: Project): Project = {
    project.settings(externalResolvers := Def.uncached { externalResolvers.value ++ resolvers })
  }

  def apply(project: Project): Project =
    project.settings(
      // GitHub Packages is a plain Maven repo (sbt-github-packages has no sbt 2
      // build). `/<org>/_` resolves packages across the org's repositories.
      // `.distinct`, not `+=`: Root/Module and With.basic both configure this
      // helper, and GithubPublishing adds the same repo again, so a plain `+=`
      // left three identical entries in `resolvers`.
      resolvers := {
        val org = RootProjectInfo.Keys.gitHubOrganization.value
        (resolvers.value :+ ("GitHub Packages" at s"https://maven.pkg.github.com/$org/_")).distinct
      },
      resolvers ++= scalaResolvers
    )
}
