package com.ossuminc.sbt.helpers

import com.github.sbt.git.GitPlugin
import com.github.sbt.git.SbtGit.useReadableConsoleGit
import sbt._

import java.io.File

object Git extends AutoPluginHelper {

  /** Check if we're in a git repository by looking for .git directory or file */
  private def isGitRepo: Boolean = {
    def checkDir(dir: File): Boolean = {
      if (dir == null) false
      else {
        val gitDir = new File(dir, ".git")
        if (gitDir.exists()) true
        else checkDir(dir.getParentFile)
      }
    }
    checkDir(new File(System.getProperty("user.dir")))
  }

  def apply(project: Project): Project = {
    val baseSettings = project.enablePlugins(GitPlugin)

    // Only use native git if we're actually in a git repository.
    // This allows scripted tests to run in non-git directories while
    // still supporting git worktrees in actual development environments.
    if (isGitRepo) {
      baseSettings.settings(
        // Use native git instead of JGit to support git worktrees.
        // JGit doesn't properly handle worktrees (sees .git file as bare repo).
        // Native git fully supports worktrees and is required for parallel
        // development with multiple Claude workers in separate worktrees.
        useReadableConsoleGit
      )
    } else {
      // Fall back to JGit for non-git directories (e.g., scripted tests)
      baseSettings
    }
  }
}
