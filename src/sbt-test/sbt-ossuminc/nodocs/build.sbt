enablePlugins(OssumIncPlugin)

lazy val root = Root("nodocs-test", startYr = 2026)
  .configure(With.noPublishing)
  .aggregate(lib)

lazy val lib = Module("lib", "nodocs-lib")
  .configure(With.typical, With.NoDocs)
  .settings(maxErrors := 50)

// Discriminating assertion: the module HAS real compile sources, but With.NoDocs
// clears the `doc` task's input sources. Fails loudly if either invariant breaks.
TaskKey[Unit]("checkNoDocs") := {
  val compileSources = (lib / Compile / sources).value
  val docSources = (lib / Compile / doc / sources).value
  println(s"NoDocs check: compileSources=${compileSources.size} docSources=${docSources.size}")
  if (compileSources.isEmpty)
    sys.error("Invalid test: expected compile sources to be present")
  if (docSources.nonEmpty)
    sys.error(s"With.NoDocs failed: doc sources not empty: $docSources")
}
