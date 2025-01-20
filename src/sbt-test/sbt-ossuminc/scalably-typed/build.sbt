import sbt.Keys.startYear
import sbt.url

import java.nio.file.{Files, Path}

enablePlugins(OssumIncPlugin)

lazy val root = Root("scalably-typed-test", startYr = 2024)
  .configure(With.typical)
  .configure(With.ScalablyTyped.withScalajsBundler(Map("chart.js" -> "4.4.3")))
