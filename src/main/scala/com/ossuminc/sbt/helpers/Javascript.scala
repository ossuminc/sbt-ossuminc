package com.ossuminc.sbt.helpers

import sbt.*
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*

object Javascript extends AutoPluginHelper {

  override def configure(project: Project): Project = {
    configure()(project)
  }

  def configure(hasMain: Boolean = false, forProd: Boolean = true, withCommonJSModule: Boolean = false)(
    project: Project
  ): Project = {
    project
      .settings(
        // for an application with a main method
        scalaJSUseMainModuleInitializer := hasMain,
        scalaJSLinkerConfig ~= {
          _.withOptimizer(forProd)
            .withModuleKind({
              if (withCommonJSModule) { ModuleKind.CommonJSModule }
              else { ModuleKind.ESModule }
            })
        }
      )

  }
}
