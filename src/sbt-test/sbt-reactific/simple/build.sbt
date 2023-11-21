import sbt.Keys.startYear
import sbt.url
import com.reactific.sbt.Scalafmt

lazy val root = (project in file("."))
.enablePlugins(ReactificPlugin)
  .configure(Scalafmt.autoUpdateScalaFmtConf())
  .settings(
    name := "hello-test",
    scalaVersion := "2.11.8",
    organization := "com.reactific",
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
