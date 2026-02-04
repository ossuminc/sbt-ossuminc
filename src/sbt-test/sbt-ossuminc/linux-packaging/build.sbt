enablePlugins(OssumIncPlugin)

lazy val root = Root("linux-packaging-test", startYr = 2026)
  .configure(With.noPublishing)
  .aggregate(hello)

lazy val hello = Program("hello", "hello", Some("Main"))
  .configure(
    With.basic,
    With.Scala3,
    With.Native(buildTarget = "application")
  )
  .configure(
    With.Packaging.linux(
      pkgName = "hello",
      pkgDescription = "Test binary for linux packaging"
    )
  )
  .settings(
    maxErrors := 50,
    TaskKey[Unit]("checkLinuxSettings") := {
      import com.ossuminc.sbt.helpers.Packaging
      val archLabel = Packaging.Keys.linuxPackageArch.value
      val osLabel = Packaging.Keys.linuxPackageOs.value
      assert(archLabel.nonEmpty, "arch label should be auto-detected")
      assert(osLabel.nonEmpty, "os label should be auto-detected")
      println(s"linux packaging configured: os=$osLabel arch=$archLabel")
    }
  )
