import sbt.Keys.startYear
import sbt.url

enablePlugins(OssumIncPlugin)

lazy val root = cro("native-test", startYr=2024)
  .configure(With.basic,With.native(buildTarget="application",debug=true,verbose=true))
  .settings(
    maxErrors := 50
  )
