import sbt.Keys.startYear
import sbt.url

enablePlugins(OssumIncPlugin)

lazy val root = Root("github-publish-test", startYr=2015)
  .configure(With.basic, With.githubPackages)
  .settings(
    maxErrors := 50,
    TaskKey[Unit]("check") := {
      println(s"Checking from within sbt:")
    }
  )
