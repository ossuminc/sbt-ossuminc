package com.ossuminc.sbt

import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.universal.UniversalDeployPlugin
import sbt.*
import sbt.Keys.{mainClass, moduleName, name, run}

object Program {

  /** Define a sub-project that produces an executable program. It is necessary to also have used the [[Root]] function
    * because what that function sets up is necessary for publishing this module.
    * @param dirName
    *   The name of the directory in which the plugin code and tests exist
    * @param appName
    *   The name of the published artifact. If blank, the `dirName` will be used
    * @return
    *   The configured sbt project that is ready to build an sbt plugin
    */
  def apply(dirName: String, appName: String, mainClazz: Option[String] = None): Project = {
    Project
      .apply(appName, file(dirName))
      .enablePlugins(OssumIncPlugin, JavaAppPackaging, UniversalDeployPlugin)
      .settings(
        name := appName,
        moduleName := { if (appName.isEmpty) dirName else appName },
        Compile / mainClass := mainClazz,
        run / mainClass := mainClazz
      )
  }
}
