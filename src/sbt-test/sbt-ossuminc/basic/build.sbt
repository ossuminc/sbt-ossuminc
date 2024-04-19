import sbt.Keys.startYear
import sbt.url

import java.nio.file.{Files,Path}

enablePlugins(OssumIncPlugin)

lazy val root = Root("basic-test", startYr=2015)
  .configure(With.basic, With.build_info)
  .settings(
    maxErrors := 50,
    TaskKey[Unit]("check") := {
      println(s"Checking from within sbt:")
    },
    TaskKey[Boolean]("checkBuildInfo") := {
      val dir = Path.of(System.getProperty("user.dir"))
      val file = dir.resolve("target/scala-2.12/src_managed/main/com/ossuminc/riddl/utils/RiddlBuildInfo.scala")
      val isReadable = Files.isReadable(file)
      println(s"Readable: $isReadable: $dir")
      isReadable
    }
  )
