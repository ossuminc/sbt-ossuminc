package com.ossuminc.sbt.helpers

import com.github.sbt.git.GitPlugin
import com.github.sbt.git.SbtGit.useReadableConsoleGit
import sbt._

object Git extends AutoPluginHelper {

  def apply(project: Project): Project = {
    project
      .enablePlugins(GitPlugin)
      .settings(
        // Use native git instead of JGit to support git worktrees.
        // JGit doesn't properly handle worktrees (sees .git file as bare repo).
        // Native git fully supports worktrees and is required for parallel
        // development with multiple Claude workers in separate worktrees.
        useReadableConsoleGit
      )
  }
}
