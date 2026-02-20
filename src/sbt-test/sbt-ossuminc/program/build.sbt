import sbt.Keys.startYear
import sbt.url

enablePlugins(OssumIncPlugin)

lazy val root = Root(
  ghRepoName = "program-test",
  ghOrgName = "ossuminc",
  orgPackage = "com.ossuminc",
  orgName = "Ossum Inc.",
  orgPage = url("https://ossuminc.com/"),
  startYr = 2024,
  devs = List(Developer("reid-spencer", "Reid Spencer", "", url("https://github.com/reid-spencer")))
)
  .configure(With.everything)
  .settings(
    name := "everything-test",
    mainClass := Some("PrintHello"),
    maxErrors := 50,
    TaskKey[Unit]("check") := {
      println(s"Checking from within sbt:")
      true
    }
  )
