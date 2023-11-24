package com.ossuminc.sbt

import com.ossuminc.sbt.helpers.Miscellaneous.buildShellPrompt

import sbt.*
import sbt.Keys.*
import sbt.librarymanagement.Resolver

object UniRepoRoot {


  def apply(id: String): Project = {
    Project
      .apply(id, file("."))
      .settings(
        name := id,
        publishArtifact := false, // no artifact to publish for the virtual root project
        publish := {}, // just to be sure
        publishLocal := {}, // and paranoid
        publishTo := Some(Resolver.defaultLocal),
        shellPrompt := buildShellPrompt.value
      )
  }
}
