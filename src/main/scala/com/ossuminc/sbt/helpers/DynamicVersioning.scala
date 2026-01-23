package com.ossuminc.sbt.helpers

import com.github.sbt.git.SbtGit.useReadableConsoleGit
import sbt.*
import sbtdynver.DynVerPlugin
import sbtdynver.DynVerPlugin.autoImport.{dynverSeparator, dynverVTagPrefix}

object DynamicVersioning extends AutoPluginHelper {

  /** The configuration function to call for this plugin helper
    *
    * @param project
    *   The project to which the configuration should be applied
    *
    * @return
    *   The same project passed as an argument, post configuration
    */
  def apply(project: Project): Project =
    project
      .enablePlugins(DynVerPlugin)
      .settings(
        // NEVER  SET  THIS:
        // version := "0.1"
        // IT IS HANDLED BY: sbt-dynver

        // don't prefix the version with a v
        ThisBuild / dynverVTagPrefix := false,

        // use the minus character to separate version fields
        ThisBuild / dynverSeparator := "-",

        // Use native git instead of JGit to support git worktrees.
        // JGit doesn't properly handle worktrees (sees .git file as bare repo).
        // Native git fully supports worktrees and is required for parallel
        // development with multiple Claude workers in separate worktrees.
        useReadableConsoleGit
      )
}
