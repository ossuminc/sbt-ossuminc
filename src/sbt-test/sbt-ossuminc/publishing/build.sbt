enablePlugins(OssumIncPlugin)

lazy val root = Root("publishing-test", startYr = 2024)
  .configure(With.basic, With.Scala3, With.Publishing)
  .settings(
    maxErrors := 50,
    // sbt 2's persistent server resolves `version` once at project load and
    // holds it, so a server left running across commits publishes artifacts
    // stamped with an OLD commit and still reports [success]. riddl published
    // 1.31.0-182-2bc257ae -- two commits stale -- exactly this way.
    //
    // Detection is deliberately conservative: only a version carrying an
    // explicit -<n>-<sha> component can be judged, so a tagged CI release
    // (which has no sha to compare) is never failed by a check that cannot
    // evaluate it.
    TaskKey[Unit]("checkStalenessLogic") := {
      import com.ossuminc.sbt.helpers.DynamicVersioning.stalenessOf
      val head = "25c4b08c"

      def mustDetect(ver: String, why: String): Unit =
        assert(
          stalenessOf(ver, head).isDefined,
          s"stalenessOf('$ver', '$head') should be flagged stale: $why"
        )
      def mustAccept(ver: String, why: String): Unit =
        assert(
          stalenessOf(ver, head).isEmpty,
          s"stalenessOf('$ver', '$head') should NOT be flagged: $why " +
            s"(got ${stalenessOf(ver, head)})"
        )

      mustDetect("1.31.0-182-2bc257ae", "the exact failure riddl reported")
      mustDetect(
        "1.31.0-182-2bc257ae-20260729-1318",
        "a dirty working tree still carries a stale sha"
      )
      mustAccept("1.31.0-184-25c4b08c", "sha matches HEAD")
      mustAccept("3.0.3", "sitting exactly on a tag -- no sha to compare")
      mustAccept("0.1.0-SNAPSHOT", "no dynver sha component at all")

      // The message must say what to do, not just that something is wrong.
      val msg = stalenessOf("1.31.0-182-2bc257ae", head).get
      assert(msg.contains("2bc257ae") && msg.contains(head), s"message lacks both SHAs: $msg")
      assert(msg.contains("reload"), s"message does not say how to fix it: $msg")
      println("checkStalenessLogic: stale/fresh version detection OK")
    },
    // isSnapshot does NOT answer "is this a prerelease?": for an RC tag dynver
    // reports isSnapshot=false, correctly, because an RC is a real immutable
    // artifact. Without these settings every consumer string-matches version.
    TaskKey[Unit]("checkPrereleaseClassification") := {
      import com.ossuminc.sbt.helpers.DynamicVersioning.{
        isPrereleaseVersion,
        isReleaseCandidateVersion,
        prereleaseIdentifier
      }

      // Empirically verified in riddl: tag 1.32.0-rc.1 -> version 1.32.0-rc.1.
      assert(isPrereleaseVersion("1.32.0-rc.1"), "an RC tag is a prerelease")
      assert(isReleaseCandidateVersion("1.32.0-rc.1"), "1.32.0-rc.1 is an RC")
      assert(isReleaseCandidateVersion("1.32.0-rc1"), "1.32.0-rc1 is an RC too")

      // dynver between-tags renderings are prereleases, but not RCs.
      assert(isPrereleaseVersion("1.32.0-4-abc12345"), "between tags is a prerelease")
      assert(
        !isReleaseCandidateVersion("1.32.0-4-abc12345"),
        "between tags is NOT a release candidate"
      )
      assert(
        isPrereleaseVersion("1.32.0-4-abc12345-20260729-1318"),
        "a dirty-tree stamp is still a prerelease"
      )

      // A plain release is neither.
      assert(!isPrereleaseVersion("1.32.0"), "1.32.0 is a release")
      assert(!isReleaseCandidateVersion("1.32.0"), "1.32.0 is not an RC")

      // Build metadata must not be mistaken for a prerelease.
      assert(!isPrereleaseVersion("1.32.0+build.5"), "+build is metadata, not prerelease")
      assert(
        prereleaseIdentifier("1.32.0-rc.1+build.5").contains("rc.1"),
        s"build metadata must be stripped, got ${prereleaseIdentifier("1.32.0-rc.1+build.5")}"
      )

      println("checkPrereleaseClassification: prerelease/RC classification OK")
    },
    // npm publish moves `latest` BY DEFAULT whatever the version says, so
    // publishing an RC without --tag makes it what `npm install <pkg>` gets.
    TaskKey[Unit]("checkDistTagRule") := {
      import com.ossuminc.sbt.helpers.NpmPublishing.distTagFor
      def expect(ver: String, tag: String): Unit =
        assert(
          distTagFor(ver) == tag,
          s"distTagFor('$ver') should be '$tag', got '${distTagFor(ver)}'"
        )
      expect("1.32.0-rc.1", "rc")
      expect("1.32.0-rc1", "rc")
      expect("1.32.0-4-abc12345", "dev")
      expect("1.32.0-4-abc12345-20260729-1318", "dev")
      expect("0.1.0-SNAPSHOT", "dev")
      expect("1.32.0", "latest")
      println("checkDistTagRule: dist-tag derivation OK")
    },
    // The escape hatch must win from anywhere -- this override is written
    // above nothing in particular, but the default lives in the plugin's
    // projectSettings, which sbt orders below all of build.sbt.
    TaskKey[Unit]("checkDistTagDefault") := {
      import com.ossuminc.sbt.helpers.NpmPublishing
      val tag = NpmPublishing.Keys.npmDistTag.value
      val ver = version.value
      val expected = NpmPublishing.distTagFor(ver)
      assert(tag == expected, s"npmDistTag for version '$ver' should be '$expected', got '$tag'")
      println(s"checkDistTagDefault: version=$ver npmDistTag=$tag")
    },
    TaskKey[Unit]("checkPrereleaseSettings") := {
      val pre = isPrerelease.value
      val rc = isReleaseCandidate.value
      val ver = version.value
      import com.ossuminc.sbt.helpers.DynamicVersioning
      assert(
        pre == DynamicVersioning.isPrereleaseVersion(ver),
        s"isPrerelease disagrees with the classifier for '$ver'"
      )
      assert(
        rc == DynamicVersioning.isReleaseCandidateVersion(ver),
        s"isReleaseCandidate disagrees with the classifier for '$ver'"
      )
      println(s"checkPrereleaseSettings: version=$ver isPrerelease=$pre isReleaseCandidate=$rc")
    }
  )
