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
      println(s"GitHub Packages publishing OK: publishTo=$pt")
    }
  )
