import sbt.Keys.startYear
import sbt.url
import com.ossuminc.sbt.OssumIncPlugin
import com.ossuminc.sbt.SingleProject

lazy val root = SingleProject("basic-test")
  .enablePlugins(OssumIncPlugin)
  .configure(With.basic)
  .settings(
    name := "basic-test",
    maxErrors := 50,
    info.Keys.projectStartYear := 2015,
    TaskKey[Unit]("check") := {
      println(s"Checking from within sbt:")
    }
  )
