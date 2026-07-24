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

// Assert CrossModule's defaultAxes yields clean project IDs (no `3` suffix).
TaskKey[Unit]("checkProjectIds") := {
  val ids = Seq(cm_jvm.id, cm_js.id, cm_native.id)
  println(s"CrossModule project IDs: ${ids.mkString(", ")}")
  val expected = Seq("cross", "crossJS", "crossNative")
  if (ids != expected)
    sys.error(s"Expected $expected but got $ids (defaultAxes regression?)")
}
