

enablePlugins(OssumIncPlugin)

lazy val root = Root("akka-test", startYr = 2015)
  .configure(With.typical, With.akka())
  .settings(maxErrors := 50)
