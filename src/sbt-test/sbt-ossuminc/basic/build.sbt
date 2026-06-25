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
    // Version-agnostic: locate the generated BuildInfo.scala anywhere under
    // target (sbt 2 uses target/out/jvm/scala-<v>/<id>/src_managed/...).
    TaskKey[Boolean]("checkBuildInfo") := {
      val base = Path.of(System.getProperty("user.dir")).resolve("target")
      val found = Files.isDirectory(base) &&
        Files.walk(base).anyMatch(p => p.getFileName.toString == "BuildInfo.scala")
      println(s"BuildInfo.scala generated under target: $found")
      if (!found) sys.error("BuildInfo.scala was not generated")
      found
    }
  )
