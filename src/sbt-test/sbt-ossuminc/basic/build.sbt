import sbt.Keys.startYear
import sbt.url
import com.ossuminc.sbt.OssumIncPlugin

lazy val root = RootProject("hello-test")
  .enablePlugins(OssumIncPlugin)
  .configure(With.basic)
  .settings(
    name := "hello-test",
    maxErrors := 50,
    unmanagedResourceDirectories in compile := {
      Seq(baseDirectory.value / "src/resources")
    },
    startYear := Some(2015),
    developerUrl := url("http://ossuminc.com/"),
    titleForDocs := "Yo!",
    codePackage := "com.ossuminc.yo",
    TaskKey[Unit]("check") := {
      if (!(baseDirectory.value / ".scalafmt.conf").exists()) {
        sys.error(".scalafmt.conf does not exist")
      }
    }
  )
