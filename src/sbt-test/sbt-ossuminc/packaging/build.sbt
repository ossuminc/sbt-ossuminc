enablePlugins(OssumIncPlugin)

lazy val root = Root("basic-test", startYr = 2015)
  .configure(With.basic, With.BuildInfo)
  .settings(
    maxErrors := 50
  )
