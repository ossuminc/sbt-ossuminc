enablePlugins(OssumIncPlugin)

lazy val root = Root("npm-packaging-test", startYr = 2026)
  .enablePlugins(ScalaJSPlugin)
  .configure(
    With.basic,
    With.ScalaJS(
      "npm-packaging-test",
      hasMain = true,
      withCommonJSModule = true
    )
  )
  .configure(
    With.Packaging.npm(
      scope = "@test",
      pkgName = "npm-packaging-test",
      pkgDescription = "Test package for npm packaging scripted test",
      keywords = Seq("test", "sbt", "scalajs"),
      esModule = false
    )
  )
  .configure(
    With.Publishing.npm(
      registries = Seq("npmjs", "github")
    )
  )
  .settings(
    scalaVersion := "3.3.7",
    maxErrors := 50,
    TaskKey[Unit]("checkPackageJson") := {
      import com.ossuminc.sbt.helpers.NpmPackaging
      val outputDir = NpmPackaging.Keys.npmOutputDir.value
      val packageJson = outputDir / "package.json"
      val content = IO.read(packageJson)
      assert(
        content.contains(""""name": "@test/npm-packaging-test""""),
        s"package.json missing expected name, got:\n$content"
      )
      assert(
        content.contains(""""license": "Apache-2.0""""),
        s"package.json missing expected license, got:\n$content"
      )
      assert(
        content.contains(""""keywords":"""),
        s"package.json missing keywords, got:\n$content"
      )
      assert(
        !content.contains(""""type": "module""""),
        s"package.json should not have type:module when esModule=false"
      )
      println("package.json content verified successfully")
    },
    TaskKey[Unit]("checkPublishingConfig") := {
      import com.ossuminc.sbt.helpers.NpmPublishing
      val regs = NpmPublishing.Keys.npmRegistries.value
      assert(
        regs == Seq("npmjs", "github"),
        s"Expected registries [npmjs, github], got: $regs"
      )
      println(s"Publishing registries configured: $regs")
    }
  )
