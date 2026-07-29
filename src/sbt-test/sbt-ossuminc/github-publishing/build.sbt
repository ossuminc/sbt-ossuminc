enablePlugins(OssumIncPlugin)

// GitHub Packages publishing is the default publishing path for every ossuminc
// project, so this exercises the With.GithubPublishing helper end-to-end: it
// must wire publishTo and a resolver to the org's GitHub Packages Maven repo.
lazy val root = Root("github-publishing-test", startYr = 2026)
  .configure(With.basic, With.Scala3, With.GithubPublishing)
  .settings(
    maxErrors := 50,
    TaskKey[Unit]("checkGithubPublishing") := {
      assert(publishMavenStyle.value, "publishMavenStyle should be true")
      val pt = publishTo.value
      assert(
        pt.exists(_.toString.contains("maven.pkg.github.com/ossuminc/github-publishing-test")),
        s"publishTo should target the GitHub Packages repo for ossuminc/github-publishing-test, got: $pt"
      )
      val res = resolvers.value
      assert(
        res.exists(_.toString.contains("maven.pkg.github.com/ossuminc/_")),
        s"resolvers should include the org GitHub Packages repo, got: $res"
      )
      // A resolver and publishTo sharing a name but not a URL makes sbt warn
      // on EVERY publish in EVERY project on this plugin:
      //   Multiple resolvers having different access mechanism configured
      //   with same name 'GitHub Packages'...
      val ptName = pt.map(_.name)
      val clashing = res.filter(r => ptName.contains(r.name) && r.toString != pt.get.toString)
      assert(
        clashing.isEmpty,
        s"publishTo resolver name ${ptName.getOrElse("")} collides with resolver(s) " +
          s"at a different URL: ${clashing.mkString(", ")}"
      )
      // Root/Module, With.basic and With.GithubPublishing each add the org
      // repo; they must not accumulate as duplicate entries.
      val dupes = res.groupBy(identity).collect { case (r, rs) if rs.size > 1 => r }
      assert(
        dupes.isEmpty,
        s"resolvers contains duplicate entries: ${dupes.mkString(", ")}"
      )
      println(s"GitHub Packages publishing OK: publishTo=$pt")
    }
  )
