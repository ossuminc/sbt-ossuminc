
enablePlugins(OssumIncPlugin)

lazy val root = Root("akka-test", startYr = 2015)
  .configure(With.typical, With.Akka.configure)
  .settings(maxErrors := 50)
