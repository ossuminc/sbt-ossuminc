package com.ossuminc.sbt.helpers

import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

/** Helper for assembling Scala.js output into an npm-publishable package.
  *
  * Provides two tasks:
  *   - `npmPrepare`: Pure sbt task that assembles the npm package directory (no npm binary
  *     required). Copies JS output, generates package.json, and optionally includes TypeScript
  *     definitions.
  *   - `npmPack`: Shells out to `npm pack` to create a `.tgz` archive.
  *
  * Usage:
  * {{{
  * .jsConfigure(
  *   With.Packaging.npm(
  *     scope = "@ossuminc",
  *     pkgName = "riddl-lib",
  *     pkgDescription = "RIDDL Language Library",
  *     keywords = Seq("riddl", "ddd", "parser"),
  *     esModule = true
  *   )
  * )
  * }}}
  *
  * TypeScript definitions are discovered by convention: if `<baseDir>/js/types/index.d.ts` exists,
  * it is copied into the package and referenced in package.json.
  *
  * Supports template mode: set `templateFile` to a `package.json` template containing
  * `VERSION_PLACEHOLDER`, which will be replaced with the project version at build time.
  */
object NpmPackaging {

  object Keys {
    val npmScope = settingKey[String](
      "npm scope (e.g. @ossuminc)"
    )
    val npmPackageName = settingKey[String](
      "npm package name (without scope)"
    )
    val npmPackageDescription = settingKey[String](
      "npm package description"
    )
    val npmKeywords = settingKey[Seq[String]](
      "npm package keywords"
    )
    val npmEsModule = settingKey[Boolean](
      "Whether to use ES module format"
    )
    val npmTemplateFile = settingKey[Option[File]](
      "Optional custom package.json template file"
    )
    val npmTypesDir = settingKey[Option[File]](
      "Directory containing TypeScript .d.ts files"
    )
    val npmOutputDir = settingKey[File](
      "Directory where npm package is assembled"
    )
    val npmPrepare = taskKey[File](
      "Assemble npm package directory (pure sbt, no npm required)"
    )
    val npmPack = taskKey[File](
      "Run npm pack to create .tgz (requires npm on PATH)"
    )
    val npmPublishLocal = taskKey[File](
      "Pack and install npm package locally to ~/.ivy2/local/npm/"
    )
  }

  /** Configure npm packaging for a Scala.js project.
    *
    * @param scope
    *   npm scope (e.g. "@ossuminc"), empty string for unscoped
    * @param pkgName
    *   npm package name (without scope)
    * @param pkgDescription
    *   Package description for package.json
    * @param keywords
    *   npm keywords for package discovery
    * @param esModule
    *   Whether to set "type": "module" in package.json
    * @param templateFile
    *   Optional package.json template with VERSION_PLACEHOLDER
    */
  def npm(
    scope: String = "",
    pkgName: String,
    pkgDescription: String = "",
    keywords: Seq[String] = Seq.empty,
    esModule: Boolean = true,
    templateFile: Option[File] = None
  )(project: Project): Project = {
    project.settings(
      Keys.npmScope := scope,
      Keys.npmPackageName := pkgName,
      Keys.npmPackageDescription := pkgDescription,
      Keys.npmKeywords := keywords,
      Keys.npmEsModule := esModule,
      Keys.npmTemplateFile := templateFile,
      Keys.npmTypesDir := {
        // Convention: look for TypeScript definitions at
        // <project-base>/js/types/
        val typesDir = baseDirectory.value / "types"
        if (typesDir.exists()) Some(typesDir) else None
      },
      Keys.npmOutputDir := target.value / "npm-package",
      Keys.npmPrepare := {
        val log = streams.value.log
        val jsOutput = (Compile / fullOptJS).value.data
        val outputDir = Keys.npmOutputDir.value
        val ver = version.value
        val scope = Keys.npmScope.value
        val name = Keys.npmPackageName.value
        val desc = Keys.npmPackageDescription.value
        val kw = Keys.npmKeywords.value
        val esm = Keys.npmEsModule.value
        val template = Keys.npmTemplateFile.value
        val typesDir = Keys.npmTypesDir.value
        val ghOrg = RootProjectInfo.Keys.gitHubOrganization.value
        val ghRepo = RootProjectInfo.Keys.gitHubRepository.value
        val lic = RootProjectInfo.Keys.spdxLicense.value

        log.info(s"Assembling npm package at $outputDir")

        // Clean and create output directory
        IO.delete(outputDir)
        IO.createDirectory(outputDir)

        // Copy optimized JS output
        IO.copyFile(jsOutput, outputDir / "main.js")
        val sourceMap = new File(jsOutput.getAbsolutePath + ".map")
        if (sourceMap.exists()) {
          IO.copyFile(sourceMap, outputDir / "main.js.map")
        }

        // Copy TypeScript definitions if present
        val hasTypes = typesDir.exists { dir =>
          val indexDts = dir / "index.d.ts"
          if (indexDts.exists()) {
            IO.copyFile(indexDts, outputDir / "index.d.ts")
            true
          } else {
            false
          }
        }

        // Generate or process package.json
        val packageJson = outputDir / "package.json"
        template match {
          case Some(tmplFile) =>
            val content = IO.read(tmplFile)
            IO.write(
              packageJson,
              content.replace("VERSION_PLACEHOLDER", ver)
            )
            log.info(s"Processed package.json template from $tmplFile")
          case None =>
            val fullName =
              if (scope.nonEmpty) s"$scope/$name" else name
            val json = generatePackageJson(
              fullName,
              ver,
              desc,
              kw,
              esm,
              hasTypes,
              ghOrg,
              ghRepo,
              lic
            )
            IO.write(packageJson, json)
            log.info("Generated package.json from settings")
        }

        // Generate README.md
        val fullName =
          if (scope.nonEmpty) s"$scope/$name" else name
        val readme = outputDir / "README.md"
        IO.write(
          readme,
          s"""# $fullName
             |
             |$desc
             |
             |## Install
             |
             |```bash
             |npm install $fullName
             |```
             |""".stripMargin
        )

        log.info(s"npm package assembled: $outputDir")
        outputDir
      },
      Keys.npmPack := {
        val log = streams.value.log
        val prepDir = Keys.npmPrepare.value
        val targetDir = target.value / "npm-packages"
        IO.createDirectory(targetDir)

        val cmd = Seq(
          "npm",
          "pack",
          s"--pack-destination=${targetDir.getAbsolutePath}"
        )
        log.info(s"Running: ${cmd.mkString(" ")}")

        val exitCode = sys.process.Process(cmd, prepDir).!
        if (exitCode != 0) {
          sys.error(s"npm pack failed with exit code $exitCode")
        }

        // Find the produced .tgz file
        val tgzFiles = (targetDir * "*.tgz").get
        if (tgzFiles.isEmpty) {
          sys.error(
            s"npm pack did not produce a .tgz file in $targetDir"
          )
        }

        val result = tgzFiles.head
        log.info(s"npm package created: $result")
        result
      },
      Keys.npmPublishLocal := {
        val log = streams.value.log
        val tgz = Keys.npmPack.value
        val scope = Keys.npmScope.value
        val name = Keys.npmPackageName.value
        val ver = version.value

        // Mirror ivy2 local convention: ~/.ivy2/local/npm/<scope>/<name>/<version>/
        val scopeDir = if (scope.nonEmpty) scope.stripPrefix("@") else "_unscoped"
        val localDir = Path.userHome / ".ivy2" / "local" / "npm" / scopeDir / name / ver
        IO.createDirectory(localDir)

        val dest = localDir / tgz.getName
        IO.copyFile(tgz, dest)

        // Also write a latest-version marker for convenience
        val latestFile = Path.userHome / ".ivy2" / "local" / "npm" / scopeDir / name / "latest.txt"
        IO.write(latestFile, ver + "\n")

        log.info(s"npm package published locally: $dest")
        log.info(s"  Install with: npm install $dest")
        dest
      },
      // Hook into publishLocal so npm .tgz is also produced
      publishLocal := {
        publishLocal.value
        Keys.npmPublishLocal.value
        ()
      }
    )
  }

  /** Generate a package.json string from settings.
    *
    * Uses string building rather than a JSON library to avoid adding dependencies (sbt plugins use
    * Scala 2.12).
    */
  private def generatePackageJson(
    fullName: String,
    version: String,
    description: String,
    keywords: Seq[String],
    esModule: Boolean,
    hasTypes: Boolean,
    ghOrg: String,
    ghRepo: String,
    license: String
  ): String = {
    val sb = new StringBuilder()
    sb.append("{\n")
    sb.append(s"""  "name": "${escapeJson(fullName)}",\n""")
    sb.append(s"""  "version": "${escapeJson(version)}",\n""")
    sb.append(s"""  "description": "${escapeJson(description)}",\n""")
    sb.append(s"""  "main": "main.js",\n""")
    if (esModule) {
      sb.append(s"""  "type": "module",\n""")
    }
    if (hasTypes) {
      sb.append(s"""  "types": "index.d.ts",\n""")
      sb.append(s"""  "exports": {\n""")
      sb.append(s"""    ".": {\n""")
      sb.append(s"""      "types": "./index.d.ts",\n""")
      sb.append(s"""      "default": "./main.js"\n""")
      sb.append(s"""    }\n""")
      sb.append(s"""  },\n""")
    }
    if (keywords.nonEmpty) {
      val kws = keywords
        .map(k => s""""${escapeJson(k)}"""")
        .mkString(", ")
      sb.append(s"""  "keywords": [$kws],\n""")
    }
    sb.append(s"""  "license": "${escapeJson(license)}",\n""")
    sb.append(s"""  "repository": {\n""")
    sb.append(s"""    "type": "git",\n""")
    sb.append(
      s"""    "url": "https://github.com/$ghOrg/$ghRepo.git"\n"""
    )
    sb.append(s"""  }\n""")
    sb.append("}\n")
    sb.toString()
  }

  /** Escape special characters for JSON string values. */
  private def escapeJson(s: String): String = {
    s.replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t")
  }
}
