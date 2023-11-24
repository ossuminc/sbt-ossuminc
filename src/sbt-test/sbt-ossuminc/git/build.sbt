import sbt.Keys.startYear
import sbt.url
import com.ossuminc.sbt.OssumIncPlugin

lazy val root = RootProject("hello-test")
  .enablePlugins(OssumIncPlugin)
  .configure(OssumIncPlugin.autoImport.With(Git, Scala2, ))
  .settings(
    name := "hello-test",
    maxErrors := 50,
    unmanagedResourceDirectories in compile := {
      Seq(baseDirectory.value / "src/resources")
    },
    copyrightHolder := "Reactific Software LLC",
    startYear := Some(2015),
    developerUrl := url("http://reactific.com/"),
    titleForDocs := "Yo!",
    codePackage := "com.reactific.yo",
    TaskKey[Unit]("check") := {
      if (!(baseDirectory.value / ".scalafmt.conf").exists()) {
        sys.error(".scalafmt.conf does not exist")
      }
    }
  )
