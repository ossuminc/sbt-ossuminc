package com.ossuminc.sbt.helpers

import sbt.*

/** Helper to configure artifact publishing.
  *
  * This provides a unified entry point for configuring publishing, defaulting
  * to GitHub Packages but allowing easy switching to Sonatype/Maven Central.
  *
  * Usage:
  * {{{
  * .configure(With.Publishing)           // Defaults to GitHub Packages
  * .configure(With.Publishing.github)    // Explicit GitHub Packages
  * .configure(With.Publishing.sonatype)  // Sonatype/Maven Central
  * }}}
  */
object Publishing extends AutoPluginHelper {

  /** Configure publishing with defaults (GitHub Packages) */
  override def apply(project: Project): Project = github(project)

  /** Configure publishing to GitHub Packages
    *
    * Requires Root() to be defined first to set gitHubOrganization and
    * gitHubRepository settings. Also requires GitHub credentials to be
    * configured in ~/.sbt/1.0/github.sbt.
    *
    * @see [[GithubPublishing]] for full configuration details
    */
  def github(project: Project): Project = GithubPublishing(project)

  /** Configure publishing to Sonatype/Maven Central
    *
    * Requires Root() to be defined first. Also requires Sonatype credentials
    * and PGP signing to be configured.
    *
    * @see [[SonatypePublishing]] for full configuration details
    */
  def sonatype(project: Project): Project = SonatypePublishing(project)

  /** Configure npm package publishing to registries.
    *
    * Delegates to [[NpmPublishing.npm]]. The project must also be
    * configured with `With.Packaging.npm(...)` to provide the
    * `npmPrepare` task.
    *
    * @param registries
    *   Target registries: `"npmjs"` and/or `"github"`
    * @see [[NpmPublishing]] for full configuration details
    */
  def npm(
    registries: Seq[String] = Seq("npmjs")
  )(project: Project): Project = {
    NpmPublishing.npm(registries)(project)
  }
}
