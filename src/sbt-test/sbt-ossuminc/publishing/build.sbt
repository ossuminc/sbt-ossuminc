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
    }
  )
