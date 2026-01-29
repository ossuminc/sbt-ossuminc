package com.ossuminc.sbt.helpers

import com.github.sbt.git.SbtGit.useReadableConsoleGit
import sbt.*
import sbtdynver.DynVerPlugin
import sbtdynver.DynVerPlugin.autoImport.{dynverSeparator, dynverVTagPrefix}

import java.io.File

object DynamicVersioning extends AutoPluginHelper {

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

  /** The configuration function to call for this plugin helper
    *
    * @param project
    *   The project to which the configuration should be applied
    *
    * @return
    *   The same project passed as an argument, post configuration
    */
  def apply(project: Project): Project = {
    val baseProject = project
      .enablePlugins(DynVerPlugin)
      .settings(
        // NEVER  SET  THIS:
        // version := "0.1"
        // IT IS HANDLED BY: sbt-dynver

        // don't prefix the version with a v
        ThisBuild / dynverVTagPrefix := false,

        // use the minus character to separate version fields
        ThisBuild / dynverSeparator := "-"
      )

    // Only use native git if we're actually in a git repository.
    // This allows scripted tests to run in non-git directories while
    // still supporting git worktrees in actual development environments.
    if (isGitRepo) {
      baseProject.settings(
        // Use native git instead of JGit to support git worktrees.
        // JGit doesn't properly handle worktrees (sees .git file as bare repo).
        // Native git fully supports worktrees and is required for parallel
        // development with multiple Claude workers in separate worktrees.
        useReadableConsoleGit
      )
    } else {
      baseProject
    }
  }
}
