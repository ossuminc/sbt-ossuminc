import sbt.Keys.startYear
import sbt.url

import java.nio.file.{Files,Path}

enablePlugins(OssumIncPlugin)

lazy val root = Root("idea-plugin-test", startYr=2015)
  .configure(With.basic, With.IdeaPlugin("TestPlugin"))
  .settings(
    maxErrors := 50
  )
