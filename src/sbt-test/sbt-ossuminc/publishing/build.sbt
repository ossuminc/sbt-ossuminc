enablePlugins(OssumIncPlugin)

lazy val root = Root("publishing-test", startYr = 2024)
  .configure(With.basic, With.Scala3, With.Publishing)
  .settings(maxErrors := 50)
