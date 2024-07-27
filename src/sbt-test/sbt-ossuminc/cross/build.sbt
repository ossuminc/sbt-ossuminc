import sbt.Keys.startYear
import sbt.url

import java.nio.file.{Files, Path}
import scala.sys.process.Process

enablePlugins(OssumIncPlugin)

lazy val cross = Root("cross")
  .configure(With.noPublishing)
  .aggregate(cm_jvm, cm_js, cm_native)

lazy val cm = CrossModule("cross", JVM, JS, Native)(With.typical)(maxErrors := 50)
val cm_jvm = cm.jvm.configure(With.typical)
val cm_js = cm.js.configure(With.typical)
val cm_native = cm.native.configure(With.typical)
