import sbt.url

enablePlugins(OssumIncPlugin)

lazy val root = Root(
  "program-test",
  startYr = 2024,
  devs = List(
    Developer(
      "reid-spencer",
      "Reid Spencer",
      "",
      url("https://github.com/reid-spencer")
    )
  )
)
  .configure(With.typical)
  .configure(With.riddl)
  .settings(
    name := "program-test",
    maxErrors := 50,
    TaskKey[Unit]("check") := {
      println(s"Checking from within sbt:")
    }
  )

lazy val program = Program("everything", "print-hello")
  .configure(With.typical)
