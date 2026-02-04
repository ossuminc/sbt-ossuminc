package com.ossuminc.sbt.helpers

import com.typesafe.sbt.SbtNativePackager.Universal
import sbt._
import sbt.Keys._

import java.security.MessageDigest

/** Helper for generating Homebrew formula files.
  *
  * Generates a `.rb` formula file suitable for inclusion in a Homebrew
  * tap repository. Supports two variants:
  *
  *   - `"universal"` (default): JVM-based, depends on
  *     `Universal/packageBin` producing a `.zip`. The formula includes
  *     an `openjdk` dependency.
  *   - `"native"`: Scala Native binary, depends on
  *     `Packaging.Keys.linuxPackage` producing a `.tar.gz`. No JDK
  *     dependency.
  *
  * The formula includes a SHA256 hash computed from the local build
  * artifact. For release workflows, this value should be verified
  * against the artifact uploaded to GitHub Releases.
  *
  * Usage:
  * {{{
  * .jvmConfigure(
  *   With.Packaging.homebrew(
  *     formulaName = "riddlc",
  *     binaryName = "riddlc",
  *     pkgDescription = "Compiler for the RIDDL language",
  *     homepage = "https://ossum.tech/riddl/"
  *   )
  * )
  * }}}
  *
  * Publishing the generated formula to a tap repository is a separate
  * git operation, not handled by this helper.
  */
object HomebrewPackaging {

  object Keys {
    val homebrewGenerate = taskKey[File](
      "Generate Homebrew formula .rb file"
    )
    val homebrewFormulaName = settingKey[String](
      "Homebrew formula name"
    )
  }

  /** Compute SHA256 hex digest of a file. */
  private def sha256(file: File): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = IO.readBytes(file)
    digest.update(bytes)
    digest.digest().map("%02x".format(_)).mkString
  }

  /** Capitalize first letter of each word for Ruby class name. */
  private def rubyClassName(name: String): String = {
    name.split("[^a-zA-Z0-9]+").map { word =>
      if (word.isEmpty) ""
      else word.head.toUpper + word.tail.toLowerCase
    }.mkString
  }

  /** Generate a Homebrew formula for a JVM universal package. */
  private def universalFormula(
    className: String,
    description: String,
    homepage: String,
    ghOrg: String,
    ghRepo: String,
    license: String,
    javaVersion: String,
    sha: String
  ): String = {
    s"""class $className < Formula
       |  desc "$description"
       |  homepage "$homepage"
       |  url "https://github.com/$ghOrg/$ghRepo/releases/download/#{version}/$className.zip"
       |  sha256 "$sha"
       |  license "$license"
       |
       |  depends_on "openjdk@$javaVersion"
       |
       |  def install
       |    libexec.install Dir["*"]
       |    bin.install_symlink Dir["#{libexec}/bin/*"]
       |  end
       |
       |  test do
       |    system "#{bin}/${className.toLowerCase}", "--help"
       |  end
       |end
       |""".stripMargin
  }

  /** Generate a Homebrew formula for a native binary. */
  private def nativeFormula(
    className: String,
    binaryName: String,
    description: String,
    homepage: String,
    ghOrg: String,
    ghRepo: String,
    license: String,
    sha: String
  ): String = {
    s"""class $className < Formula
       |  desc "$description"
       |  homepage "$homepage"
       |  url "https://github.com/$ghOrg/$ghRepo/releases/download/#{version}/$binaryName.tar.gz"
       |  sha256 "$sha"
       |  license "$license"
       |
       |  def install
       |    bin.install "$binaryName"
       |  end
       |
       |  test do
       |    system "#{bin}/$binaryName", "--help"
       |  end
       |end
       |""".stripMargin
  }

  /** Configure Homebrew formula generation.
    *
    * @param formulaName
    *   Formula name (used as Ruby class name, lowercase for file)
    * @param binaryName
    *   Binary executable name (used in formula test and install)
    * @param pkgDescription
    *   Description shown in `brew info`
    * @param homepage
    *   Project homepage URL
    * @param javaVersion
    *   Required JDK version (universal variant only)
    * @param tapRepo
    *   GitHub tap repo (e.g. "ossuminc/homebrew-tap"), for
    *   documentation purposes only — publishing is not automated
    * @param variant
    *   "universal" (JVM, default) or "native" (Scala Native binary)
    */
  def homebrew(
    formulaName: String,
    binaryName: String,
    pkgDescription: String = "",
    homepage: String = "",
    javaVersion: String = "25",
    tapRepo: String = "",
    variant: String = "universal"
  )(project: Project): Project = {
    val className = rubyClassName(formulaName)

    // Build the task definition at build-definition time based on
    // variant, so that only the correct .value references are
    // resolved by sbt (sbt resolves ALL .value calls in a task
    // body regardless of runtime control flow).
    val taskDef: Def.Initialize[Task[File]] = variant match {
      case "universal" =>
        Def.task {
          val log = streams.value.log
          val ver = version.value
          val ghOrg = RootProjectInfo.Keys.gitHubOrganization.value
          val ghRepo = RootProjectInfo.Keys.gitHubRepository.value
          val lic = RootProjectInfo.Keys.spdxLicense.value
          val artifact = (Universal / sbt.Keys.packageBin).value
          val sha = sha256(artifact)

          log.info(s"Computed SHA256 for ${artifact.getName}: $sha")

          val formula = universalFormula(
            className, pkgDescription, homepage,
            ghOrg, ghRepo, lic, javaVersion, sha
          )
          writeFormula(
            formulaName, formula, artifact.getName,
            ver, tapRepo, target.value, log
          )
        }

      case "native" =>
        Def.task {
          val log = streams.value.log
          val ver = version.value
          val ghOrg = RootProjectInfo.Keys.gitHubOrganization.value
          val ghRepo = RootProjectInfo.Keys.gitHubRepository.value
          val lic = RootProjectInfo.Keys.spdxLicense.value
          val artifact = Packaging.Keys.linuxPackage.value
          val sha = sha256(artifact)

          log.info(s"Computed SHA256 for ${artifact.getName}: $sha")

          val formula = nativeFormula(
            className, binaryName, pkgDescription, homepage,
            ghOrg, ghRepo, lic, sha
          )
          writeFormula(
            formulaName, formula, artifact.getName,
            ver, tapRepo, target.value, log
          )
        }

      case other =>
        sys.error(
          s"Unknown homebrew variant: '$other'. " +
          "Supported: \"universal\", \"native\""
        )
    }

    project.settings(
      Keys.homebrewFormulaName := formulaName,
      Keys.homebrewGenerate := taskDef.value
    )
  }

  /** Write the formula file and log details. */
  private def writeFormula(
    formulaName: String,
    formula: String,
    artifactDesc: String,
    ver: String,
    tapRepo: String,
    targetDir: File,
    log: Logger
  ): File = {
    val formulaDir = targetDir / "homebrew" / "Formula"
    IO.createDirectory(formulaDir)
    val formulaFile = formulaDir / s"$formulaName.rb"
    IO.write(formulaFile, formula)
    log.info(s"Generated Homebrew formula: $formulaFile")
    log.info(s"  Artifact: $artifactDesc")
    log.info(s"  Version: $ver")
    if (tapRepo.nonEmpty) {
      log.info(
        s"  Publish to tap: copy $formulaFile to $tapRepo"
      )
    }
    formulaFile
  }
}
