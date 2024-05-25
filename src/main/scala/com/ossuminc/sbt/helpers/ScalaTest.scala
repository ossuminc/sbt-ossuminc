package com.ossuminc.sbt.helpers
import sbt.*
import sbt.Keys.libraryDependencies
import sbt.Project
import sbt.librarymanagement.ModuleID

object ScalaTest extends AutoPluginHelper {

  object V {
    val scalacheck = "1.17.0"
    val scalatest = "3.2.18"
  }
  val scalactic = "org.scalactic" %% "scalactic" % V.scalatest
  val scalatest = "org.scalatest" %% "scalatest" % V.scalatest
  val scalacheck = "org.scalacheck" %% "scalacheck" % V.scalacheck


  override def configure(project: Project): Project = {

    project.settings(
      libraryDependencies ++= Seq[ModuleID](
        scalactic % Test, scalatest % Test, scalacheck % Test)
    )
  }
}
