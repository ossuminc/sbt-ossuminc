enablePlugins(OssumIncPlugin)

lazy val root = Root("sonatype-test", startYr = 2026)
  .configure(With.noPublishing)
  .aggregate(rel, snap)

// Release version -> publishTo should be the built-in localStaging resolver.
lazy val rel = Module("rel", "rel")
  .configure(With.basic, With.Scala3, With.SonatypePublishing)
  .settings(
    version := "1.0.0",
    isSnapshot := false, // deterministic in scripted temp dir (no git tag)
    maxErrors := 50,
    TaskKey[Unit]("checkSonatypeRelease") := {
      assert(publishMavenStyle.value, "publishMavenStyle should be true")
      val pt = publishTo.value
      assert(
        pt.exists(_.name == "local-staging"),
        s"release publishTo should be localStaging (name local-staging), got: $pt"
      )
      val scm = scmInfo.value
      assert(
        scm.exists(_.browseUrl.toString.contains("github.com/ossuminc/sonatype-test")),
        s"scmInfo should reference the GitHub repo, got: $scm"
      )
      println(s"Sonatype Central Portal release publishTo OK: ${pt.map(_.name)}")
    }
  )

// Snapshot version -> publishTo should be the Central Portal snapshots repo.
lazy val snap = Module("snap", "snap")
  .configure(With.basic, With.Scala3, With.SonatypePublishing)
  .settings(
    version := "1.0.0-SNAPSHOT",
    isSnapshot := true, // deterministic in scripted temp dir (no git tag)
    maxErrors := 50,
    TaskKey[Unit]("checkSonatypeSnapshot") := {
      val pt = publishTo.value
      assert(
        pt.exists(_.name == "central-snapshots"),
        s"snapshot publishTo should be central-snapshots, got: $pt"
      )
      assert(
        pt.exists(_.toString.contains("central.sonatype.com")),
        s"snapshot publishTo should target central.sonatype.com, got: $pt"
      )
      println(s"Sonatype Central Portal snapshot publishTo OK: $pt")
    }
  )
