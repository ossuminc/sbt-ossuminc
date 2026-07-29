enablePlugins(OssumIncPlugin)

// `With.Scala3` (and therefore `With.typical`) defines a PROJECT-level
// `scalacOptions`. sbt delegates the project axis LAST, so an undefined
// `<proj> / Test / scalacOptions` finds that project-level setting before it
// ever reaches `ThisBuild / Test / scalacOptions` -- making the line below
// silently ineffective and forcing the option to be repeated per project.
// riddl hit this adding a -Wconf rule for test sources.
ThisBuild / Test / scalacOptions += "-Wconf:msg=SENTINEL-THISBUILD:s"

lazy val root = Root("test-scalacoptions-test", startYr = 2026)
  .configure(With.basic, With.Scala3)
  .settings(
    maxErrors := 50,
    // The plugin's own project-level options must survive.
    TaskKey[Unit]("checkProjectOptionsIntact") := {
      val opts = (Compile / scalacOptions).value
      Seq("-deprecation", "-feature", "-new-syntax").foreach { o =>
        assert(opts.contains(o), s"project-level scalacOptions lost $o, got: $opts")
      }
      println(s"checkProjectOptionsIntact: ${opts.mkString(" ")}")
    },
    // The point of the test.
    TaskKey[Unit]("checkThisBuildTestOptions") := {
      val opts = (Test / scalacOptions).value
      println(s"checkThisBuildTestOptions: Test / scalacOptions = ${opts.mkString(" ")}")
      assert(
        opts.contains("-Wconf:msg=SENTINEL-THISBUILD:s"),
        "ThisBuild / Test / scalacOptions was silently ignored -- the project-level " +
          s"scalacOptions from With.Scala3 shadowed it. Got: $opts"
      )
      // Test sources must still get the project-level options too.
      assert(
        opts.contains("-deprecation"),
        s"Test / scalacOptions lost the project-level options, got: $opts"
      )
      // Honouring ThisBuild must not duplicate options.
      assert(
        opts == opts.distinct,
        s"Test / scalacOptions contains duplicates: ${opts.diff(opts.distinct).mkString(", ")}"
      )
    },
    // The explicit helper, for projects that would rather state it outright.
    TaskKey[Unit]("checkTestScalacOptionsHelper") := {
      val opts = (Test / scalacOptions).value
      assert(
        opts.contains("-Wconf:msg=SENTINEL-HELPER:s"),
        s"With.testScalacOptions did not take effect, got: $opts"
      )
      println("checkTestScalacOptionsHelper: helper option present")
    }
  )
  .configure(With.testScalacOptions("-Wconf:msg=SENTINEL-HELPER:s"))
