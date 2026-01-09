enablePlugins(OssumIncPlugin)

lazy val root = Module("mima-test", "mima-test")
  .configure(
    With.typical,
    With.MiMa("1.0.0")
  )
  .settings(
    version := "1.0.1",
    maxErrors := 50
  )
