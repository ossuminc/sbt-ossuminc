maxErrors := 5

val commonSettings = Seq(
  copyrightHolder := "Reactific Software LLC",
  startYear := Some(2015),
  developerUrl := url("http://reactific.com/"),
  titleForDocs := "Yo!",
  codePackage := "com.reactific.yo"
)

lazy val p1 = (project in file("p1"))
  .enablePlugins(ReactificPlugin)
  .settings(commonSettings)

lazy val p2 = (project in file("p2"))
  .enablePlugins(ReactificPlugin)
  .dependsOn(p1)
  .settings(commonSettings)
