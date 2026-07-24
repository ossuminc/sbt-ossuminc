import sbt.Keys.startYear
import sbt.url

import java.nio.file.{Files, Path}
import scala.sys.process.Process

enablePlugins(OssumIncPlugin)

lazy val cross = Root("cross_test")
  .configure(With.noPublishing)
  .aggregate(cm_jvm, cm_js, cm_native, nojs_jvm, nojs_native)

// JS-targeting module: shared code uses @JSExportTopLevel (cross/src/main/...).
// It compiles on all three rows only if CrossModule auto-adds scalajs-stubs to
// the JVM and Native rows — with NO stubs dep declared here.
lazy val cm = CrossModule("cross", "cross_test.cross")(JVM, JS, Native)
   .configure(With.typical)
   .settings(maxErrors := 50)
val cm_jvm = cm.jvm
val cm_js = cm.js
val cm_native = cm.native

// Control module: does NOT target JS, so it must NOT get scalajs-stubs.
lazy val nojs = CrossModule("nojs", "cross_test.nojs")(JVM, Native)
   .configure(With.typical)
   .settings(maxErrors := 50)
val nojs_jvm = nojs.jvm
val nojs_native = nojs.native

// Assert CrossModule's defaultAxes yields clean project IDs (no `3` suffix).
TaskKey[Unit]("checkProjectIds") := {
  val ids = Seq(cm_jvm.id, cm_js.id, cm_native.id)
  println(s"CrossModule project IDs: ${ids.mkString(", ")}")
  val expected = Seq("cross", "crossJS", "crossNative")
  if (ids != expected)
    sys.error(s"Expected $expected but got $ids (defaultAxes regression?)")
}

// A JS-targeting cross module auto-adds scalajs-stubs to its JVM + Native rows.
TaskKey[Unit]("checkHasStubs") := Def.uncached {
  val jvmHas = (cm_jvm / libraryDependencies).value.exists(_.name.startsWith("scalajs-stubs"))
  val natHas = (cm_native / libraryDependencies).value.exists(_.name.startsWith("scalajs-stubs"))
  println(s"checkHasStubs: jvm=$jvmHas native=$natHas")
  if (!jvmHas) sys.error("JVM row is missing auto-added scalajs-stubs")
  if (!natHas) sys.error("Native row is missing auto-added scalajs-stubs")
}

// A non-JS cross module must NOT be polluted with scalajs-stubs.
TaskKey[Unit]("checkNoStubs") := Def.uncached {
  val jvmHas = (nojs_jvm / libraryDependencies).value.exists(_.name.startsWith("scalajs-stubs"))
  val natHas = (nojs_native / libraryDependencies).value.exists(_.name.startsWith("scalajs-stubs"))
  println(s"checkNoStubs: jvm=$jvmHas native=$natHas")
  if (jvmHas || natHas) sys.error("non-JS module wrongly got scalajs-stubs")
}
