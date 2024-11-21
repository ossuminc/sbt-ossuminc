package com.ossuminc.sbt.helpers

import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.SbtNativePackager.Docker
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.graalvmnativeimage.GraalVMNativeImagePlugin
import com.typesafe.sbt.packager.Keys.*
import com.typesafe.sbt.packager.graalvmnativeimage.GraalVMNativeImagePlugin.autoImport.graalVMNativeImageCommand
import sbt.*

import java.io.File


object Packaging extends AutoPluginHelper {

  override def configure(project: Project): Project = universal()(project)

  def universal()(project: Project): Project = project

  def docker(
              maintainerEmail: String,
              pkgName: String = "",
              pkgSummary: String = "",
              pkgDescription: String = "",
              dockerFile: File = file(""),
            )(project: Project): Project = {
    project
      .enablePlugins(SbtNativePackager, DockerPlugin)
      .settings(
        maintainer := maintainerEmail,
        ThisBuild / maintainer := maintainerEmail,
        Docker / maintainer := maintainerEmail,
        Docker / packageName := pkgName,
        Docker / packageSummary := pkgSummary,
        Docker / packageDescription := pkgDescription
      )
  }

  def graalVM(pkgName: String, pkgSummary: String, native_image_path: File)(project: Project): Project = {
    project.enablePlugins(SbtNativePackager, GraalVMNativeImagePlugin)
      .settings(
        packageName := pkgName,
        packageSummary := pkgSummary,
        graalVMNativeImageCommand := native_image_path.getAbsolutePath
      )
  }

  def jdkPackager()(project: Project): Project = project

  def linuxDebian()(project: Project): Project = project

  def linuxRPM()(project: Project): Project = project
}