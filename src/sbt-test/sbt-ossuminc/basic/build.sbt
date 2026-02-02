import sbt.Keys.startYear
import sbt.url

import java.nio.file.{Files,Path}

enablePlugins(OssumIncPlugin)

lazy val root = Root("basic-test", startYr=2015)
  .configure(With.basic, With.BuildInfo)
  .settings(
    maxErrors := 50,
    TaskKey[Unit]("check") := {
      println(s"Checking from within sbt:")
    },
    TaskKey[Boolean]("checkBuildInfo") := {
      val dir = Path.of(System.getProperty("user.dir"))
      // sbt 2.x uses unified target directory: target/out/jvm/scala-X.Y.Z/project-name/
      val file = dir.resolve("target/out/jvm/scala-3.7.4/basic-test/src_managed/main/com/ossuminc/BuildInfo.scala")
      val isReadable = Files.isReadable(file)
      println(s"Readable: $isReadable: $file")
      isReadable
    }
  )
