import sbt.Keys.startYear
import sbt.url

import java.nio.file.{Files, Path}
import scala.sys.process.Process

enablePlugins(OssumIncPlugin)

lazy val cross = Root("cross_test")
  .configure(With.noPublishing)
  .aggregate(cm_jvm, cm_js, cm_native)

lazy val cm = CrossModule("cross", "cross_test.cross")(JVM, JS, Native)
   .configure(With.typical)
   .settings(maxErrors := 50)
val cm_jvm = cm.jvm
val cm_js = cm.js
val cm_native = cm.native
