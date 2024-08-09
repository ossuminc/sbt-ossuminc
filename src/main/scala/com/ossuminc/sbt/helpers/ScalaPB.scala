package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.{libraryDependencies, *}
import sbt.Project
import sbtprotoc.ProtocPlugin.autoImport.*


object ScalaPB extends AutoPluginHelper {

  override def configure(project: Project): Project = {

    project.settings(
      Compile / PB.targets := Seq(
        scalapb.gen(
          singleLineToProtoString = false,
          asciiFormatToString = true,
          lenses  = false,
          retainSourceCodeInfo = false
        ) -> (Compile / sourceManaged).value / "scalapb"
      ),
      libraryDependencies ++= Seq(
        "com.google.protobuf" % "protobuf-java" % "3.13.0" % "protobuf",
        "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.0",
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",

    )
  }
}
