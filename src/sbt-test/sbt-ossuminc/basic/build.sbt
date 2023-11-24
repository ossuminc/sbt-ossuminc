import sbt.Keys.startYear
import sbt.url
import com.ossuminc.sbt.OssumIncPlugin
import com.ossuminc.sbt.SingleProject
import com.ossuminc.sbt.OssumIncPlugin.autoImport._

lazy val root = SingleProject("hello-test")
  .enablePlugins(OssumIncPlugin)
  .configure(With.basic)
  .settings(
    name := "hello-test",
    maxErrors := 50,
    info.Keys.codePackage := "com.ossuminc.basic",
    info.Keys.projectStartYear := 2015,
    TaskKey[Unit]("check") := {
      if (!(baseDirectory.value / ".scalafmt.conf").exists()) {
        sys.error(".scalafmt.conf does not exist")
      }
    }
  )
