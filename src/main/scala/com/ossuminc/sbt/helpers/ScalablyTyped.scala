package com.ossuminc.sbt.helpers

import org.scalablytyped.converter.Selection
import org.scalablytyped.converter.plugin.STKeys.{stMinimize, stSourceGenMode}
import org.scalablytyped.converter.plugin.ScalablyTypedPluginBase.autoImport.*
import org.scalablytyped.converter.plugin.{ScalablyTypedConverterGenSourcePlugin, SourceGenMode}
import sbt.*
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.{npmDependencies, useYarn}

/** An AutoPluginHelper to make using ScalablyTyped easier  */
object ScalablyTyped extends AutoPluginHelper {

  override def configure(project: Project): Project = apply(Map.empty)(project)

  def apply(
     dependencies: Map[String,String],
     useNPM: Boolean = true,
     useScalaJsDom: Boolean = false,
     allTransitives: Boolean = true,
     exceptions: List[String] = List.empty[String],
     ignore: List[String] = List.empty[String],
     outputPackage: String = "org.ossum.sauce",
     withDebugOutput: Boolean = false
  )(project: Project): Project = {
    project
      .enablePlugins(ScalablyTypedConverterGenSourcePlugin)
      .settings(
        Compile / npmDependencies ++= dependencies.toSeq,
        useYarn := !useNPM,
        stSourceGenMode := SourceGenMode.ResourceGenerator,
        Compile / stMinimize := {
          if (allTransitives) {
            if (exceptions.isEmpty) Selection.All else Selection.AllExcept(exceptions:_*)
          } else {
            if (exceptions.isEmpty) Selection.None else Selection.NoneExcept(exceptions:_*)
          }
        },
        stIgnore ++= ignore,
        stOutputPackage := outputPackage,
        stUseScalaJsDom := useScalaJsDom,
        stQuiet := !withDebugOutput
    )
  }
}
