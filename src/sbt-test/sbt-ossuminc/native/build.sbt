import sbt.Keys.startYear
import sbt.url

import java.nio.file.{Files, Path}

enablePlugins(OssumIncPlugin)

lazy val root = Root("native", startYr = 2024)
  .configure(With.basic, With.build_info, With.native(noLTO = true))
  .settings(
    maxErrors := 50,
    TaskKey[Boolean]("checkBuildInfo") := {
      val dir = Path.of(System.getProperty("user.dir"))
      val file = dir.resolve("target/scala-2.12/src_managed/main/com/ossuminc/riddl/utils/RiddlBuildInfo.scala")
      val isReadable = Files.isReadable(file)
      println(s"Readable: $isReadable: $dir")
      isReadable
    }
  )
