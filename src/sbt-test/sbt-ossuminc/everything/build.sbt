import sbt.Keys.startYear
import sbt.url
import com.ossuminc.sbt.OssumIncPlugin
import com.ossuminc.sbt.Root

lazy val root = Root("everything-test", false)
  .enablePlugins(OssumIncPlugin)
  .configure(With.everything)
  .settings(
    name := "everything-test",
    maxErrors := 50,
    info.Keys.projectStartYear := 2015,
    TaskKey[Unit]("check") := {
      println(s"Checking from within sbt:")
      if (!(baseDirectory.value / ".scalafmt.conf").exists()) {
        sys.error(".scalafmt.conf does not exist")
      }
    }
  )
