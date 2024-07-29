package com.ossuminc.sbt

import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbt.Keys.{moduleName, name}
import sbt.{Project, file}

object Module {

  /** Define a sub-project or module of the root project. Make sure to use the [[Root]] function before this Module is
    * defined. No configuration is applied but you can do that by using the various With.* functions in this plugin.
    * `With.typical` is typical for Scala3 development
    * @param dirName
    *   The name of the sub-directory in which the module is located.
    * @param modName
    *   The name of the artifact to be published. If blank, it will default to the dirName
    * @return
    *   The project that was created and configured.
    */
  def apply(dirName: String, modName: String = ""): Project = {
    Project
      .apply(dirName, file(dirName))
      .enablePlugins(OssumIncPlugin, JavaAppPackaging)
      .settings(
        name := dirName,
        moduleName := { if (modName.isEmpty) dirName else modName }
      )
  }
}
