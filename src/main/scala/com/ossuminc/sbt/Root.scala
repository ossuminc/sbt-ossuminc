package com.ossuminc.sbt

import sbt.{Developer, Project, file, url}
import java.net.URI

/** A configuration object for a root project of a monorepo composed of
  * subordinate sbt projects
  */
object Root {

  /** Define a Root level project whether it is for a single-project repo or
    * a unirepo with many subprojects. This project is configured with a shell
    * prompt, and the standard project information at ThisBuild scope.
    *
    * @param ghRepoName
    *   The name of the GitHub repository we are building
    * @param ghOrgName
    *   The name of the GitHub organization/user that contains the `ghRepoName`
    * @param orgPackage
    *   The organization part of the JVM package this repo uses for its code
    * @param orgName
    *   The legal name of the organization
    * @param orgPage
    *   The URL of the website to use for the organization
    * @param startYr
    *   The year in which this project started, for copyright purposes
    * @param devs
    *   A list of Developer specifications to include in POM (required by Maven)
    * @param spdx
    *   The SPDX license identifier (e.g., "Apache-2.0", "MIT")
    * @param projectId
    *   The sbt project ID (default: "root")
    * @return
    *   The project that was created and configured.
    */
  def apply(
    ghRepoName: String = "",
    ghOrgName: String = "ossuminc",
    orgPackage: String = "com.ossuminc",
    orgName: String = "Ossum, Inc.",
    orgPage: URI = new URI("https://ossuminc.com/"),
    startYr: Int = 2023,
    devs: List[Developer] = List.empty,
    spdx: String = "Apache-2.0",
    projectId: String = "root"
  ): Project = {
    Project
      .apply(projectId, file(System.getProperty("user.dir")))
      .enablePlugins(OssumIncPlugin)
      .configure(
        helpers.RootProjectInfo.initialize(
          ghRepoName,
          ghOrgName,
          startYr,
          orgPackage,
          orgName,
          orgPage,
          devs,
          spdx
        ),
        helpers.Resolvers
      )
  }
}
