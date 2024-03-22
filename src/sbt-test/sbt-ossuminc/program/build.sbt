import sbt.Keys.startYear
import sbt.url

enablePlugins(OssumIncPlugin)

lazy val root = Root("everything-test",
  org = "com.reactific",
  orgName = "Reactific Software LLC",
  orgPage = url("https://reactific.com/"),
  startYr = 2014,
  devs = List(Developer(
      "reid-spencer",
      "Reid Spencer",
      "",
      url("https://github.com/reid-spencer")
  ))
  )
  .configure(With.everything)
  .settings(
    name := "everything-test",
    maxErrors := 50,
    TaskKey[Unit]("check") := {
      println(s"Checking from within sbt:")
    }
  )
