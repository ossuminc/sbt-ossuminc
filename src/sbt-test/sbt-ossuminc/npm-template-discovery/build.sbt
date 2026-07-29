enablePlugins(OssumIncPlugin)

// A package.json.template at the conventional location must be discovered and
// used WITHOUT passing templateFile to Packaging.npm.
//
// Before 3.1.0, templateFile defaulted to None and nothing ever looked for a
// template, so a well-formed package.json.template sitting in a repo (riddl had
// one for riddlLib) looked authoritative and was completely inert.
//
// The point of this build is that it passes NO templateFile.

lazy val root = Root("npm-template-discovery-test", startYr = 2026)
  .enablePlugins(ScalaJSPlugin)
  .configure(
    With.basic,
    With.ScalaJS(
      "npm-template-discovery-test",
      hasMain = true,
      withCommonJSModule = true
    )
  )
  .configure(
    With.Packaging.npm(
      scope = "@test",
      pkgName = "npm-template-discovery",
      pkgDescription = "This description must NOT appear -- the template wins",
      keywords = Seq("test", "sbt", "scalajs"),
      esModule = false
    )
  )
  .settings(
    scalaVersion := "3.8.4",
    maxErrors := 50,
    TaskKey[Unit]("checkTemplateDiscovered") := Def.uncached {
      import com.ossuminc.sbt.helpers.NpmPackaging
      val tmpl = NpmPackaging.Keys.npmTemplateFile.value
      println(s"checkTemplateDiscovered: npmTemplateFile=$tmpl")
      tmpl match {
        case None =>
          sys.error(
            "npmTemplateFile is None -- package.json.template at the project " +
              "base was not discovered"
          )
        case Some(f) =>
          if (!f.exists()) sys.error(s"npmTemplateFile points at a nonexistent file: $f")
      }
    },
    TaskKey[Unit]("checkTemplateUsed") := Def.uncached {
      import com.ossuminc.sbt.helpers.NpmPackaging
      val outputDir = NpmPackaging.Keys.npmOutputDir.value
      val content = IO.read(outputDir / "package.json")
      if (!content.contains(""""marker": "came-from-template""""))
        sys.error(s"package.json did not come from the template, got:\n$content")
      // VERSION_PLACEHOLDER must have been substituted.
      if (content.contains("VERSION_PLACEHOLDER"))
        sys.error(s"VERSION_PLACEHOLDER was not substituted, got:\n$content")
      // The settings-generated document must not have been used.
      if (content.contains("This description must NOT appear"))
        sys.error(s"settings-generated package.json overrode the template:\n$content")
      println("checkTemplateUsed: template was used and version substituted")
    }
  )
