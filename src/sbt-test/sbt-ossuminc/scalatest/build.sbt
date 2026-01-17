enablePlugins(OssumIncPlugin)

lazy val root = Root("scalatest-test", startYr = 2024)
  .configure(With.basic, With.Scala3, With.Scalatest())
  .settings(maxErrors := 50)
