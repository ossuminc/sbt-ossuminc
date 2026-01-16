
enablePlugins(OssumIncPlugin)

lazy val root = Root("akka-test", startYr = 2015)
  .configure(With.typical)
  .configure(With.Akka.forRelease("25.10"))
  .settings(maxErrors := 50)
