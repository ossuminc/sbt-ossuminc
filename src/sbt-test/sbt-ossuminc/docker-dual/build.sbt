import sbt.Keys.startYear
import sbt.url

enablePlugins(OssumIncPlugin)

lazy val root = Root(
  ghRepoName = "docker-dual-test",
  ghOrgName = "ossuminc",
  orgPackage = "com.ossuminc",
  orgName = "Ossum Inc.",
  orgPage = url("https://ossuminc.com/"),
  startYr = 2026,
  devs = List(Developer("reid-spencer", "Reid Spencer", "", url("https://github.com/reid-spencer")))
)
  .configure(With.typical)
  .configure(
    With.Packaging.dockerDual(
      mainClass = "Main",
      pkgName = "docker-dual-test",
      exposedPorts = Seq(8080, 9001)
    )
  )
  .settings(
    name := "docker-dual-test",
    maxErrors := 50,
    // Custom check task to verify docker-dual settings
    TaskKey[Unit]("checkDockerDual") := {
      import com.ossuminc.sbt.helpers.Packaging
      val log = streams.value.log
      
      // Verify settings are configured correctly
      val baseImg = dockerBaseImage.value
      val repo = dockerRepository.value
      val user = dockerUsername.value
      val ports = dockerExposedPorts.value
      val mainCls = Packaging.Keys.dockerMainClass.value
      val devBase = Packaging.Keys.dockerDevBaseImage.value
      val prodBase = Packaging.Keys.dockerProdBaseImage.value
      
      log.info(s"dockerBaseImage: $baseImg")
      log.info(s"dockerRepository: $repo")
      log.info(s"dockerUsername: $user")
      log.info(s"dockerExposedPorts: $ports")
      log.info(s"dockerMainClass: $mainCls")
      log.info(s"dockerDevBaseImage: $devBase")
      log.info(s"dockerProdBaseImage: $prodBase")
      
      // Assertions
      assert(baseImg == "eclipse-temurin:25-jdk-noble", s"Expected dev base image, got: $baseImg")
      assert(repo == Some("us-central1-docker.pkg.dev"), s"Expected Artifact Registry repository, got: $repo")
      assert(user == Some("ossuminc-production/ossum-images"), s"Expected Artifact Registry project/repo path, got: $user")
      assert(ports == Seq(8080, 9001), s"Expected ports 8080,9001, got: $ports")
      assert(mainCls == "Main", s"Expected Main class, got: $mainCls")
      assert(devBase == "eclipse-temurin:25-jdk-noble", s"Unexpected dev base: $devBase")
      assert(prodBase == "gcr.io/distroless/java25-debian13:nonroot", s"Unexpected prod base: $prodBase")
      
      log.info("All docker-dual settings verified!")
    }
  )
