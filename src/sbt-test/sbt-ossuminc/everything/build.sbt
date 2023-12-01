import sbt.Keys.startYear
import sbt.url

enablePlugins(OssumIncPlugin)

lazy val root = Root("everything-test")
  .configure(With.everything)
  .settings(
    name := "everything-test",
    maxErrors := 50,
    With.project_info.Keys.projectStartYear := 2015,
    TaskKey[Unit]("check") := {
      println(s"Checking from within sbt:")
    }
  )
