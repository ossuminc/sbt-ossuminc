enablePlugins(OssumIncPlugin)

lazy val root = Root("basic-test", startYr = 2015)
  .configure(With.basic, With.build_info)
  .settings(
    maxErrors := 50
  )
