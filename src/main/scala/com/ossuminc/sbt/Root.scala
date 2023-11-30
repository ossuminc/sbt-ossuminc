package com.ossuminc.sbt

import com.ossuminc.sbt.helpers.Miscellaneous.buildShellPrompt

import sbt.*
import sbt.Keys.*
import sbt.librarymanagement.Resolver

object Root {

  def apply(id: String, hasSubProjects: Boolean = true): Project = {
    val project = Project
      .apply(id, file("."))
      .settings(
        name := id,
        Global / shellPrompt := buildShellPrompt.value
      )
    if (hasSubProjects) {
      project.settings(
        publishArtifact := false, // no artifact to publish for the virtual root project
        publish := {}, // just to be sure
        publishLocal := {}, // and paranoid
        publishTo := Some(Resolver.defaultLocal)
      )
    } else
      project
  }
}

object Module {

  def apply(id: String, dirName: String): Project = {
    Project
      .apply(id, file(dirName))
      .settings(
        name := id
      )
  }
}
