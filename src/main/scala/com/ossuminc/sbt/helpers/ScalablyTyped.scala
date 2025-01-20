package com.ossuminc.sbt.helpers

import sbt.*
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.{npmDependencies, useYarn}

import org.scalablytyped.converter.Selection
import org.scalablytyped.converter.plugin.STKeys.{externalNpm, stMinimize, stSourceGenMode}
import org.scalablytyped.converter.plugin.ScalablyTypedPluginBase.autoImport.*
import org.scalablytyped.converter.plugin.{
  ScalablyTypedConverterExternalNpmPlugin,
  ScalablyTypedConverterGenSourcePlugin,
  SourceGenMode
}

import scala.sys.process.Process

/** An AutoPluginHelper to make using ScalablyTyped easier */
object ScalablyTyped extends AutoPluginHelper {

  override def configure(project: Project) = withoutScalajsBundler()(project)
  
  def withoutScalajsBundler(
    packageJsonDir: File = file("."),
    useScalaJsDom: Boolean = false,
    allTransitives: Boolean = true,
    exceptions: List[String] = List.empty[String],
    ignore: List[String] = List.empty[String],
    outputPackage: String = "org.ossum.sauce",
    withDebugOutput: Boolean = false
  )(project: Project): Project = {
    val package_json_dir = packageJsonDir.getAbsoluteFile
    val newProj = project
      .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
      .settings(
        externalNpm := {
          Process("npm install", package_json_dir).!
          package_json_dir
        }
      )
    configure(useScalaJsDom, allTransitives, exceptions, ignore, outputPackage, withDebugOutput)(
      newProj
    )
  }

  def withScalajsBundler(
    dependencies: Map[String, String],
    useNPM: Boolean = true,
    useScalaJsDom: Boolean = false,
    allTransitives: Boolean = true,
    exceptions: List[String] = List.empty[String],
    ignore: List[String] = List.empty[String],
    outputPackage: String = "org.ossum.sauce",
    withDebugOutput: Boolean = false
  )(project: Project): Project = {
    val newproj = project
      .enablePlugins(ScalablyTypedConverterGenSourcePlugin)
      .settings(
        useYarn := !useNPM,
        Compile / npmDependencies ++= dependencies.toSeq
      )
    configure(useScalaJsDom, allTransitives, exceptions, ignore, outputPackage, withDebugOutput)(
      newproj
    )
  }

  private def configure(
    useScalaJsDom: Boolean,
    allTransitives: Boolean,
    exceptions: List[String],
    ignore: List[String],
    outputPackage: String,
    withDebugOutput: Boolean
  )(project: Project): Project = {
    project.settings(
      stSourceGenMode := SourceGenMode.ResourceGenerator,
      Compile / stMinimize := {
        if (allTransitives) {
          if (exceptions.isEmpty) Selection.All else Selection.AllExcept(exceptions: _*)
        } else {
          if (exceptions.isEmpty) Selection.None else Selection.NoneExcept(exceptions: _*)
        }
      },
      stIgnore ++= ignore,
      stOutputPackage := outputPackage,
      stUseScalaJsDom := useScalaJsDom,
      stQuiet := !withDebugOutput
    )
  }
}
