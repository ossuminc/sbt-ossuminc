package com.ossuminc.sbt

import sbt.{Developer, Project, URL, file, url}

/** A configuration object for a root project of a monorepo composed of subordinate sbt projects */
object Root {

  /** Define a Root level project whether it is for a single-project repo or a unirepo with many sub-projects. This
    * project is configured with a shell prompt, and the standard project information at ThisBuild scope
    * @param ghRepoName
    *   The name of the github repository we are building
    * @param ghOrgName
    *   THe name of the github organization/user that contains the `ghRepoName`
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
    * @return
    *   The project that was created and configured.
    */
  def apply(
    ghRepoName: String = "",
    ghOrgName: String = "ossuminc",
    orgPackage: String = "com.ossuminc",
    orgName: String = "Ossum, Inc.",
    orgPage: URL = url("https://ossuminc.com/"),
    maintainerEmail: String = "reid@ossuminc.com",
    startYr: Int = 2023,
    devs: List[Developer] = List.empty,
    spdx: String = "Apache-2.0"
  ): Project = {
    Project
      .apply("root", file(System.getProperty("user.dir")))
      .enablePlugins(OssumIncPlugin)
      .configure(
        helpers.RootProjectInfo.initialize(
          ghRepoName,
          ghOrgName,
          startYr,
          orgPackage,
          orgName,
          orgPage,
          maintainerEmail,
          devs,
          spdx
        ),
        helpers.Resolvers.configure
      )
  }
}
