enablePlugins(OssumIncPlugin)

lazy val root = Root("mima-test", startYr = 2024)
  .configure(With.noPublishing)
  .aggregate(lib)

lazy val lib = Module("lib", "mima-lib")
  .configure(
    With.typical,
    With.MiMa("1.0.0")
  )
  .settings(
    version := "1.0.1",
    maxErrors := 50
  )
