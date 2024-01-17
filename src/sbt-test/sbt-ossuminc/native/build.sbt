import sbt.Keys.startYear
import sbt.url

enablePlugins(OssumIncPlugin)

lazy val root = Root("native-test", startYr=2024)
  .configure(With.basic,With.native(debug=false))
  .settings(
    maxErrors := 50
  )
