import java.nio.file.{Files, Path}

enablePlugins(OssumIncPlugin)

lazy val root = Root("hello", startYr = 2024)
  .configure(With.noPublishing)
  .aggregate(hello)
  .settings(
    TaskKey[Boolean]("checkBuildInfo", "Check existence of BuildInfo.scala") := {
      val dir = Path.of(System.getProperty("user.dir"))
      val file = dir.resolve("hello/target/scala-3.4.3/src_managed/main/com/ossuminc/BuildInfo.scala")
      val isReadable = Files.isReadable(file)
      println(s"Readable: $isReadable: $dir")
      isReadable
    },
    TaskKey[Boolean]("printMarker", "") := {
      println("=== Marker ===")
      true
    }
  )

lazy val hello = Program("hello", "hello", Some("test.hello"))
  .configure(With.basic, With.build_info, With.scala3, With.native(buildTarget = "application"))
