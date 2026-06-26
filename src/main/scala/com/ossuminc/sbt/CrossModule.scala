package com.ossuminc.sbt

import sbt.*
import sbt.Keys.*

/** A CrossModule is a module that can be built for JVM, ScalaJS and/or Native.
  *
  * sbt 2.x: this is implemented on the built-in `projectMatrix` (which replaces
  * the sbt-crossproject `crossProject` builder used in the sbt 1.x line). Use it
  * like:
  * {{{
  * lazy val cm = CrossModule("dir", "artifact")(JVM, JS, Native)
  *   .configure(With.typical)
  *   .settings(...)
  *   .jvmConfigure(With.coverage(50))
  *   .jsConfigure(With.ScalaJS("..."))
  *   .nativeConfigure(With.Native(mode = "fast"))
  * lazy val cm_jvm    = cm.jvm
  * lazy val cm_js     = cm.js
  * lazy val cm_native = cm.native
  * }}}
  *
  * `projectMatrix` needs the Scala version(s) at platform-declaration time, so
  * the version is a parameter of [[apply]] (default 3.3.7, matching With.Scala3).
  * It is also used to resolve the per-platform sub-projects in `.jvm`/`.js`/
  * `.native`, so it must match the version configured by `With.typical`/
  * `With.Scala3` on this module.
  */
object CrossModule {
  sealed trait Target
  case object JVMTarget extends Target
  case object JSTarget extends Target
  case object NativeTarget extends Target

  // Aliases exposed via OssumIncPlugin.autoImport
  val JVM: Target = JVMTarget
  val JS: Target = JSTarget
  val Native: Target = NativeTarget

  private type Xform = Project => Project

  /** Default Scala version — matches [[com.ossuminc.sbt.helpers.Scala3]]. */
  val defaultScalaVersion: String = "3.3.7"

  /** Define a cross-platform subproject. Configuration is deferred (accumulated)
    * and materialized into a `projectMatrix` the first time `.jvm`/`.js`/
    * `.native` is accessed, so both whole-module (`configure`/`settings`) and
    * per-platform (`jvmConfigure`/`jsConfigure`/`nativeConfigure`) customization
    * compose the way the sbt 1.x CrossProject-based API did.
    *
    * @param dirName  subdirectory containing the module
    * @param modName  published artifact name (defaults to `dirName` if blank)
    * @param scalaVersion  Scala version for the platform axes (default 3.3.7)
    */
  def apply(
    dirName: String,
    modName: String = "",
    scalaVersion: String = defaultScalaVersion
  )(targets: Target*): CrossModule =
    new CrossModule(dirName, modName, scalaVersion, targets, Nil, Nil, Nil, Nil, Nil)
}

import CrossModule.Target

/** Immutable builder around an sbt 2 `projectMatrix`. See [[CrossModule]]. */
final class CrossModule private[sbt] (
  dirName: String,
  modName: String,
  scalaVersion: String,
  targets: Seq[Target],
  common: Seq[Project => Project],
  commonSettings: Seq[Def.SettingsDefinition],
  jvmXforms: Seq[Project => Project],
  jsXforms: Seq[Project => Project],
  nativeXforms: Seq[Project => Project]
) {

  private def copy(
    common: Seq[Project => Project] = common,
    commonSettings: Seq[Def.SettingsDefinition] = commonSettings,
    jvmXforms: Seq[Project => Project] = jvmXforms,
    jsXforms: Seq[Project => Project] = jsXforms,
    nativeXforms: Seq[Project => Project] = nativeXforms
  ): CrossModule =
    new CrossModule(
      dirName, modName, scalaVersion, targets,
      common, commonSettings, jvmXforms, jsXforms, nativeXforms
    )

  /** Apply configuration function(s) to every platform of this module. */
  def configure(transforms: (Project => Project)*): CrossModule =
    copy(common = common ++ transforms)

  /** Apply settings to every platform of this module. */
  def settings(ss: Def.SettingsDefinition*): CrossModule =
    copy(commonSettings = commonSettings ++ ss)

  /** Apply configuration only to the JVM platform. */
  def jvmConfigure(transform: Project => Project): CrossModule =
    copy(jvmXforms = jvmXforms :+ transform)

  /** Apply configuration only to the Scala.js platform. */
  def jsConfigure(transform: Project => Project): CrossModule =
    copy(jsXforms = jsXforms :+ transform)

  /** Apply configuration only to the Scala Native platform. */
  def nativeConfigure(transform: Project => Project): CrossModule =
    copy(nativeXforms = nativeXforms :+ transform)

  /** Apply settings only to the JVM platform. */
  def jvmSettings(ss: Def.SettingsDefinition*): CrossModule =
    jvmConfigure(_.settings(ss*))

  /** Apply settings only to the Scala.js platform. */
  def jsSettings(ss: Def.SettingsDefinition*): CrossModule =
    jsConfigure(_.settings(ss*))

  /** Apply settings only to the Scala Native platform. */
  def nativeSettings(ss: Def.SettingsDefinition*): CrossModule =
    nativeConfigure(_.settings(ss*))

  private def chain(xs: Seq[Project => Project]): Project => Project =
    xs.foldLeft(identity[Project])(_ andThen _)

  /** The materialized projectMatrix (built once, lazily). */
  lazy val matrix: ProjectMatrix = {
    val mname = if (modName.isEmpty) dirName else modName
    var m = ProjectMatrix(dirName, file(dirName), getClass.getClassLoader)
      .enablePlugins(OssumIncPlugin)
      .settings(name := dirName, moduleName := mname)
      .settings(commonSettings*)
      .configure(common*)
    if (targets.contains(CrossModule.JVMTarget))
      m = m.jvmPlatform(Seq(scalaVersion), Seq.empty[VirtualAxis], chain(jvmXforms))
    if (targets.contains(CrossModule.JSTarget))
      m = m.jsPlatform(Seq(scalaVersion), Seq.empty[VirtualAxis], chain(jsXforms))
    if (targets.contains(CrossModule.NativeTarget)) {
      // scalatest-core_native (3.2.19) pins an older scala-native test-interface
      // than the active scala-native (0.5.x), tripping strict eviction. Downgrade
      // to a warning so the newer, compatible test-interface is selected.
      val nativeDefaults: Project => Project =
        _.settings(evictionErrorLevel := Level.Warn)
      m = m.nativePlatform(
        Seq(scalaVersion),
        Seq.empty[VirtualAxis],
        nativeDefaults.andThen(chain(nativeXforms))
      )
    }
    m
  }

  /** The JVM sub-project (requires JVM in the target list). */
  def jvm: Project = matrix.jvm.apply(scalaVersion)

  /** The Scala.js sub-project (requires JS in the target list). */
  def js: Project = matrix.js.apply(scalaVersion)

  /** The Scala Native sub-project (requires Native in the target list). */
  def native: Project = matrix.native.apply(scalaVersion)
}
