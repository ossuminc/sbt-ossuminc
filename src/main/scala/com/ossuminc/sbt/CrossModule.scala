package com.ossuminc.sbt

/** STUB: cross-platform (JVM/JS/Native) modules are not yet wired on sbt 2.x.
  *
  * sbt 2 replaces the sbt-crossproject `crossProject` builder (and the
  * portable-scala plugins) with the built-in `projectMatrix`. CrossModule will
  * be reimplemented on `projectMatrix` in a follow-up — see NOTEBOOK.md
  * "sbt 2.0 Migration Plan", Phase 1b. Until then, use `Module(...)` for
  * JVM-only modules.
  *
  * The `Target` ADT and `JVM`/`JS`/`Native` values are kept so that
  * `OssumIncPlugin.autoImport` and consumer build files still type-check.
  */
object CrossModule {
  sealed trait Target
  case object JVMTarget extends Target
  case object JSTarget extends Target
  case object NativeTarget extends Target

  // Backward-compatible aliases used via OssumIncPlugin.autoImport
  val JVM: Target = JVMTarget
  val JS: Target = JSTarget
  val Native: Target = NativeTarget

  /** Not yet available on sbt 2.x — fails fast with an explanatory message
    * rather than producing a broken cross-build. Reimplementation tracked in
    * NOTEBOOK Phase 1b (projectMatrix).
    */
  def apply(dirName: String, modName: String = "")(targets: Target*): Nothing =
    sys.error(
      "CrossModule is not yet available on sbt 2.x: it is being reimplemented on " +
        "the built-in projectMatrix (see NOTEBOOK 'sbt 2.0 Migration Plan', " +
        "Phase 1b). Use Module(...) for JVM-only modules in the meantime."
    )
}
