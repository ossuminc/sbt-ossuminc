import sbt.Keys.startYear
import sbt.url

import java.nio.file.{Files,Path}

enablePlugins(OssumIncPlugin)

lazy val root = Root("akka-test", startYr=2015)
  .configure(With.scala3, With.akka, With.publishing)
  .settings(maxErrors := 50)
