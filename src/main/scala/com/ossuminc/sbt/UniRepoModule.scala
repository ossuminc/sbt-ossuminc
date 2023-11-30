package com.ossuminc.sbt

import com.ossuminc.sbt.helpers.Miscellaneous.buildShellPrompt
import sbt.*
import sbt.Keys.*

object UniRepoModule {

  def apply(id: String): Project = {
    Project
      .apply(id, file("."))
      .settings(
        name := id,
        shellPrompt := buildShellPrompt.value
      )
  }
}
