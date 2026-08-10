enablePlugins(OssumIncPlugin)

// Regression test for: `npmPack` returned an arbitrary tarball from
// `target/npm-packages`, which accumulated one per version ever built. A fresh
// `target` cannot reproduce the bug, so this build seeds stale tarballs first.

lazy val root = Root("npm-pack-freshness", startYr = 2026)
  .enablePlugins(ScalaJSPlugin)
  .configure(
    With.basic,
    With.ScalaJS(
      "npm-pack-freshness",
      hasMain = true,
      withCommonJSModule = true
    )
  )
  .configure(
    With.Packaging.npm(
      scope = "@test",
      pkgName = "npm-pack-freshness",
      pkgDescription = "Test package for npm pack freshness scripted test",
      keywords = Seq("test", "sbt", "scalajs"),
      esModule = false
    )
  )
  .settings(
    scalaVersion := "3.8.4",
    // Pinned so the expected tarball name is deterministic. npm derives it as
    // <scope-without-@>-<name>-<version>.tgz with `/` flattened to `-`.
    version := "0.2.0",
    maxErrors := 50,
    TaskKey[Unit]("seedStaleTarballs") := {
      val dir = target.value / "npm-packages"
      IO.createDirectory(dir)
      // Names that sort both BEFORE and AFTER the real one, so a `.head` over
      // the glob cannot pick correctly by accident of ordering.
      val stale = Seq("0.0.1", "0.1.0", "0.1.9", "0.3.0", "9.9.9")
      stale.foreach { v =>
        IO.write(dir / s"test-npm-pack-freshness-$v.tgz", s"stale tarball $v")
      }
      println(s"seeded ${stale.size} stale tarballs in $dir")
    },
    TaskKey[Unit]("checkPackFresh") := {
      import com.ossuminc.sbt.helpers.NpmPackaging
      val tgz = NpmPackaging.Keys.npmPack.value
      val ver = version.value
      val expected = s"test-npm-pack-freshness-$ver.tgz"
      assert(
        tgz.getName == expected,
        s"npmPack returned a stale tarball: got ${tgz.getName}, expected $expected"
      )
      val remaining = ((target.value / "npm-packages") * "*.tgz").get()
      assert(
        remaining.size == 1,
        "npmPack must not let target/npm-packages accumulate tarballs; found: " +
          remaining.map(_.getName).sorted.mkString(", ")
      )
      val packaged = sys.process
        .Process(
          Seq("tar", "-xzOf", tgz.getAbsolutePath, "package/package.json")
        )
        .!!
      assert(
        packaged.contains(s""""version": "$ver""""),
        s"packaged package.json is not version $ver:\n$packaged"
      )
      println(s"npmPack returned the freshly produced tarball: ${tgz.getName}")
    },
    TaskKey[Unit]("checkPublishLocalFresh") := {
      import com.ossuminc.sbt.helpers.NpmPackaging
      val dest = NpmPackaging.Keys.npmPublishLocal.value
      val ver = version.value
      assert(
        dest.getName == s"test-npm-pack-freshness-$ver.tgz",
        s"npmPublishLocal wrote a stale filename: ${dest.getName} (version is $ver)"
      )
      assert(
        dest.getParentFile.getName == ver,
        s"npmPublishLocal used the wrong version directory: ${dest.getParentFile}"
      )
      val published = sys.process
        .Process(
          Seq("tar", "-xzOf", dest.getAbsolutePath, "package/package.json")
        )
        .!!
      assert(
        published.contains(s""""version": "$ver""""),
        s"published package.json is not version $ver:\n$published"
      )
      println(s"npmPublishLocal published a matching artifact: $dest")
    }
  )
