enablePlugins(OssumIncPlugin)

lazy val root = Root("homebrew-test", startYr = 2026)
  .configure(With.noPublishing)
  .aggregate(app)

lazy val app = Program("app", "app", Some("Main"))
  .configure(With.basic, With.Scala3)
  .configure(
    With.Packaging.homebrew(
      formulaName = "test-app",
      binaryName = "test-app",
      pkgDescription = "Test application for homebrew scripted test",
      homepage = "https://example.com/test-app"
    )
  )
  .settings(
    maxErrors := 50,
    TaskKey[Unit]("checkFormula") := {
      val formulaFile = target.value / "homebrew" / "Formula" / "test-app.rb"
      assert(formulaFile.exists(), s"Formula not found at $formulaFile")
      val content = IO.read(formulaFile)
      assert(
        content.contains("class TestApp < Formula"),
        s"Formula missing class definition, got:\n$content"
      )
      assert(
        content.contains("""desc "Test application for homebrew scripted test""""),
        s"Formula missing description, got:\n$content"
      )
      assert(
        content.contains("""homepage "https://example.com/test-app""""),
        s"Formula missing homepage, got:\n$content"
      )
      assert(
        content.contains("""depends_on "openjdk@25""""),
        s"Formula missing JDK dependency, got:\n$content"
      )
      assert(
        content.contains("""license "Apache-2.0""""),
        s"Formula missing license, got:\n$content"
      )
      assert(
        content.contains("sha256"),
        s"Formula missing SHA256, got:\n$content"
      )
      println("Homebrew formula content verified successfully")
    }
  )
