package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.libraryDependencies
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*

object Javascript extends AutoPluginHelper {

  override def configure(project: Project): Project = {
    configure()(project)
  }

  def configure(
    header: String = "no header",
    hasMain: Boolean = false,
    forProd: Boolean = true,
    withCommonJSModule: Boolean = false
  )(
    project: Project
  ): Project = {
    project
      .enablePlugins(ScalaJSPlugin)
      // .settings(ScalaJSPlugin.testConfigSettings) <-- generates undefined settings
      .settings(
        // for an application with a main method
        scalaJSUseMainModuleInitializer := hasMain,
        scalaJSLinkerConfig ~= {
          _.withOptimizer(forProd)
            .withModuleKind({
              if (withCommonJSModule) {
                ModuleKind.CommonJSModule
              } else {
                ModuleKind.ESModule
              }
            })
            .withSourceMap(!forProd)
            .withJSHeader("// " + header + "\n")
        },
        libraryDependencies ++= Seq(
          "io.github.cquiroz" %%% "scala-java-time" % "2.6.0",
          "org.scalactic" %%% "scalactic" % "3.2.19" % "test",
          "org.scalatest" %%% "scalatest" % "3.2.19" % "test",
          "org.scalatest" %%% "scalatest-funspec" % "3.2.19" % "test"
        )
      )

  }
}
