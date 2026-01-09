
enablePlugins(OssumIncPlugin)

lazy val root = Root("akka-test", startYr = 2015)
  .configure(With.typical)
  .configure(With.Akka.forRelease("24.10", sys.env.get("AKKA_REPO_TOKEN")))
  .settings(maxErrors := 50)
