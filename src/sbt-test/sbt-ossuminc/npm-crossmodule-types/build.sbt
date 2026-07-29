enablePlugins(OssumIncPlugin)

// Regression test for the npmTypesDir convention under sbt 2's projectMatrix.
//
// Under the sbt 1 CrossProject layout the JS row was based at <mod>/js, so
// `baseDirectory / "types"` found <mod>/js/types. Under projectMatrix the row's
// baseDirectory is the SYNTHETIC .sbt/matrix/<rowId>/, so that lookup silently
// resolved to a directory that can never exist: npmTypesDir was None, no
// index.d.ts was packaged, and package.json omitted "types"/"exports" with no
// diagnostic. @ossuminc/riddl-lib shipped without TypeScript definitions for an
// entire major version this way.
//
// The point of this build is that it declares NO npmTypesDir override.

lazy val root = Root("npm-crossmodule-types-test", startYr = 2026)
  .configure(With.noPublishing)
  .aggregate(mylib_jvm, mylib_js)

lazy val mylib = CrossModule("mylib", "npm-crossmodule-types-test.mylib")(JVM, JS)
  .configure(With.typical)
  .settings(maxErrors := 50)
  .jsConfigure(
    With.ScalaJS(
      "npm-crossmodule-types-test",
      hasMain = false,
      withCommonJSModule = true
    )
  )
  .jsConfigure(
    With.Packaging.npm(
      scope = "@test",
      pkgName = "npm-crossmodule-types-test",
      pkgDescription = "Test package for npmTypesDir discovery under projectMatrix",
      keywords = Seq("test", "sbt", "scalajs"),
      esModule = true
    )
  )
val mylib_jvm = mylib.jvm
val mylib_js = mylib.js

// The convention must resolve to the REAL source tree, not the matrix row's
// synthetic baseDirectory.
TaskKey[Unit]("checkTypesDirFound") := Def.uncached {
  import com.ossuminc.sbt.helpers.NpmPackaging
  val dir = (mylib_js / NpmPackaging.Keys.npmTypesDir).value
  val base = (mylib_js / baseDirectory).value
  println(s"checkTypesDirFound: npmTypesDir=$dir")
  println(s"checkTypesDirFound: row baseDirectory=$base")
  dir match {
    case None =>
      sys.error(
        "npmTypesDir is None -- the convention did not find mylib/js/types. " +
          s"It most likely looked under the synthetic row base $base"
      )
    case Some(d) =>
      if (!d.exists())
        sys.error(s"npmTypesDir points at a nonexistent directory: $d")
      if (!(d / "index.d.ts").exists())
        sys.error(s"npmTypesDir has no index.d.ts: $d")
      if (d.getAbsolutePath.contains(".sbt/matrix"))
        sys.error(s"npmTypesDir resolved into the synthetic matrix tree: $d")
  }
}

// npmPrepare must actually stage index.d.ts and reference it in package.json.
// (Checks the prepared directory rather than the .tgz so the test does not
// require an npm binary -- npm pack packs exactly this directory.)
TaskKey[Unit]("checkTypesPackaged") := Def.uncached {
  import com.ossuminc.sbt.helpers.NpmPackaging
  val outputDir = (mylib_js / NpmPackaging.Keys.npmOutputDir).value
  val dts = outputDir / "index.d.ts"
  if (!dts.exists())
    sys.error(s"index.d.ts was not staged into the npm package at $outputDir")

  val content = IO.read(outputDir / "package.json")
  if (!content.contains(""""types": "index.d.ts""""))
    sys.error(s"""package.json is missing "types", got:\n$content""")
  if (!content.contains(""""exports":"""))
    sys.error(s"""package.json is missing "exports", got:\n$content""")
  println(s"checkTypesPackaged: index.d.ts staged and declared in package.json")
}

// Parity items the hand-written templates carried but the generator dropped.
TaskKey[Unit]("checkGeneratedParity") := Def.uncached {
  import com.ossuminc.sbt.helpers.NpmPackaging
  val outputDir = (mylib_js / NpmPackaging.Keys.npmOutputDir).value
  val content = IO.read(outputDir / "package.json")
  def require(fragment: String, why: String): Unit =
    if (!content.contains(fragment))
      sys.error(s"package.json is missing $why ($fragment), got:\n$content")

  // Without "files", npm packs whatever is in the staging dir -- correct only
  // by accident.
  require(""""files":""", "the files array")
  require(""""index.d.ts"""", "index.d.ts in files")
  // ESM consumers resolve "import"; "default" stays as the fallback.
  require(""""import": "./main.js"""", "exports import condition")
  require(""""default": "./main.js"""", "exports default condition")
  // A scoped package is private by default on npmjs without this.
  require(""""publishConfig":""", "publishConfig")
  require(""""access": "public"""", "publishConfig.access")
  require(""""homepage":""", "homepage")
  require(""""bugs":""", "bugs")
  println("checkGeneratedParity: files/exports/publishConfig/homepage/bugs present")
}
