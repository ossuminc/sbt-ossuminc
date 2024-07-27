package com.ossuminc.sbt.helpers

import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt.*
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*

object Javascript extends AutoPluginHelper {

  override def configure(project: Project): Project = {
    configure()(project)
  }

  def configure(hasMain: Boolean = false, forProd: Boolean = false)(project: Project): Project = {
    project
      .enablePlugins(ScalaJSPlugin)
      .settings(
        // for an application with a main method
        scalaJSUseMainModuleInitializer := hasMain,
        scalaJSLinkerConfig ~= {
          _.withOptimizer(forProd)
            .withModuleKind(ModuleKind.CommonJSModule)
        }
      )

  }
}
