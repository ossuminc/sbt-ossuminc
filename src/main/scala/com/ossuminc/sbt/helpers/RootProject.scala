package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*
import sbt.librarymanagement.Resolver

import java.io.File

object RootProject {

  private def currBranch: String = {
    import com.github.sbt.git.JGit
    val jgit = JGit(new File("."))
    jgit.branch
  }

  private[sbt] def buildShellPrompt: Def.Initialize[State => String] = {
    Def.setting { (state: State) =>
      val id = Project.extract(state).currentProject.id
      s"${name.value}($id) : $currBranch : ${version.value}>"
    }
  }

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
