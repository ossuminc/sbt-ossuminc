package com.ossuminc.sbt

import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbt.Keys.{moduleName, name}
import sbt.*

/** A regular software library module. */
object Module {

  /** Define a subproject or module of the root project. Make sure to use the [[Root]] function before this Module is
    * defined. No configuration is applied but you can do that by using the various With.* functions in this plugin.
    * `With.typical` is typical for Scala3 development
    * @param dirName
    *   The name of the subdirectory in which the module is located.
    * @param modName
    *   The name of the artifact to be published. If blank, it will default to the dirName
    * @return
    *   The project that was created and configured.
    */
  def apply(dirName: String, modName: String = ""): Project = {
    require(dirName.nonEmpty, "You must provide a directory name")
    val mname = {
      if (modName.isEmpty) {
        require(dirName != ".", "modName cannot default to '.', please specify it directly")
        dirName
      } else modName
    }
    val identity = if (dirName == ".") mname else dirName
    Project
      .apply(identity, file(dirName))
      .enablePlugins(OssumIncPlugin, JavaAppPackaging)
      .settings(
        name := identity,
        moduleName := mname
      )
  }
}
