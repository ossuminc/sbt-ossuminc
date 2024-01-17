package com.ossuminc.sbt.helpers
import sbt.*
import sbt.Keys.*

import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport.*

// import to add Scala Native options
import scala.scalanative.build._

object Native extends AutoPluginHelper {

  /** The configuration function to call for this plugin helper to
    * add support for Scala Native
    *
    * @param project
    * The project to which the configuration should be applied
    *
    * @return
    * The same project passed as an argument, post configuration
    */
  def configure(project: Project): Project = {
    configure()(project)
  }

  def configure(debugLog: Boolean = true )(project: Project): Project = {
    project
      .enablePlugins(ScalaNativePlugin)
      .settings(
        // set to Debug for compilation details (Info is default)
        logLevel := {
          if (debugLog) { Level.Debug } else  { Level.Info }
        },
        scalaVersion := "3.3.1",
        // defaults set with common options shown
        nativeConfig ~= { c =>
          c.withLTO(LTO.none) // thin
            .withMode(Mode.debug) // releaseFast
            .withGC(GC.immix) // commix
        }
      )
  }
}
