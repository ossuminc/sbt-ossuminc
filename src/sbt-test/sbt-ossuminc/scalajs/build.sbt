import sbt.Keys.startYear
import sbt.url

import java.nio.file.{Files, Path}

enablePlugins(OssumIncPlugin)

lazy val root = Root("scalajs", startYr = 2024)
  .enablePlugins(ScalaJSPlugin)
  .configure(With.basic, With.js("test main", hasMain = true, withCommonJSModule=true))
  .settings(
    scalaVersion:= "3.4.2",
    maxErrors := 50,
    TaskKey[Unit]("check") := {
      println(s"Checking from within sbt:")
    }
  )
