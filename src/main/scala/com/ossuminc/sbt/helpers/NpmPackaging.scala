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
  * TypeScript definitions are discovered by convention: the first of `<moduleDir>/js/types/` or
  * `<moduleDir>/types/` that exists is used, and an `index.d.ts` found there is copied into the
  * package and referenced in package.json. `npmPrepare` logs which directory was used, or every
  * location it searched when none was found.
  *
  * `<moduleDir>` is the module's real source tree, derived from `sourceDirectory` rather than
  * `baseDirectory` so that the convention works on `projectMatrix` rows, whose `baseDirectory` is a
  * synthetic `.sbt/matrix/<rowId>/`. Pass `typesDir` explicitly to override the convention.
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
    * @param typesDir
    *   Explicit TypeScript definitions directory; overrides the `<moduleDir>/js/types` convention
    */
  def npm(
    scope: String = "",
    pkgName: String,
    pkgDescription: String = "",
    keywords: Seq[String] = Seq.empty,
    esModule: Boolean = true,
    templateFile: Option[File] = None,
    typesDir: Option[File] = None
  )(project: Project): Project = {
    project.settings(
      Keys.npmScope := scope,
      Keys.npmPackageName := pkgName,
      Keys.npmPackageDescription := pkgDescription,
      Keys.npmKeywords := keywords,
      Keys.npmEsModule := esModule,
      Keys.npmTemplateFile := {
        templateFile.orElse(templateCandidates(moduleBaseDirectory.value).find(_.exists()))
      },
      Keys.npmTypesDir := {
        typesDir.orElse(typesDirCandidates(moduleBaseDirectory.value).find(_.exists()))
      },
      Keys.npmOutputDir := target.value / "npm-package",
      Keys.npmPrepare := Def.uncached {
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
        val searched = typesDirCandidates(moduleBaseDirectory.value)
        val ghOrg = RootProjectInfo.Keys.gitHubOrganization.value
        val ghRepo = RootProjectInfo.Keys.gitHubRepository.value
        val lic = RootProjectInfo.Keys.spdxLicense.value
        val author = organizationName.value

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

        // Copy TypeScript definitions if present. Report the outcome either
        // way: a silently-empty result here is how an entire major version
        // shipped without .d.ts files and nobody noticed.
        val hasTypes = typesDir match {
          case Some(dir) =>
            val indexDts = dir / "index.d.ts"
            if (indexDts.exists()) {
              IO.copyFile(indexDts, outputDir / "index.d.ts")
              log.info(s"TypeScript definitions: $indexDts")
              true
            } else {
              log.warn(
                s"npmTypesDir is $dir but it has no index.d.ts; " +
                  "package will ship without TypeScript definitions"
              )
              false
            }
          case None =>
            log.warn(
              "no TypeScript definitions found (looked in " +
                searched.mkString(", ") + "); package will ship without them"
            )
            false
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
            log.info(
              s"Using package.json template: $tmplFile " +
                "(template wins; settings-generated package.json not used)"
            )
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
              sourceMap.exists(),
              ghOrg,
              ghRepo,
              lic,
              author
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
      Keys.npmPack := Def.uncached {
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
        val tgzFiles = (targetDir * "*.tgz").get()
        if (tgzFiles.isEmpty) {
          sys.error(
            s"npm pack did not produce a .tgz file in $targetDir"
          )
        }

        val result = tgzFiles.head
        log.info(s"npm package created: $result")
        result
      },
      Keys.npmPublishLocal := Def.uncached {
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

  /** The module's real base directory, valid on both plain projects and `projectMatrix` rows.
    *
    * A matrix row's `baseDirectory` is a *synthetic* `.sbt/matrix/<rowId>/` directory that contains
    * no sources, so conventions anchored on it silently find nothing. `sourceDirectory`, however,
    * is set by projectMatrix to `<realModuleBase>/src`, so its parent is the module's actual source
    * tree root. On a plain (non-matrix) project the same identity holds trivially.
    *
    * This is why the `<mod>/js/types` convention broke in the sbt 1 -> sbt 2 migration: under sbt
    * 1's `CrossProject` the JS row really was based at `<mod>/js`, so `baseDirectory / "types"` was
    * correct.
    */
  private def moduleBaseDirectory: Def.Initialize[File] =
    Def.setting(sourceDirectory.value.getParentFile)

  /** Conventional locations for TypeScript definitions, most specific first.
    *
    *   - `<mod>/js/types` — a cross-platform module's JS-only sources
    *   - `<mod>/types` — a single-platform Scala.js project
    */
  private def typesDirCandidates(moduleBase: File): Seq[File] =
    Seq(moduleBase / "js" / "types", moduleBase / "types")

  /** Conventional locations for a `package.json` template, most specific first.
    *
    * Auto-discovered when `Packaging.npm(...)` is given no explicit `templateFile`, so that a
    * template sitting in the repo actually governs packaging instead of looking authoritative while
    * being inert.
    */
  private def templateCandidates(moduleBase: File): Seq[File] =
    Seq(
      moduleBase / "js" / "package.json.template",
      moduleBase / "package.json.template"
    )

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
    hasSourceMap: Boolean,
    ghOrg: String,
    ghRepo: String,
    license: String,
    author: String
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
      // `import` is what a bundler resolves for an ESM consumer; `default`
      // remains as the universal fallback. The hand-written template this
      // generator replaces declared only `import`, the generator only
      // `default` -- emit both rather than let the drift stand.
      sb.append(s"""      "import": "./main.js",\n""")
      sb.append(s"""      "default": "./main.js"\n""")
      sb.append(s"""    }\n""")
      sb.append(s"""  },\n""")
    }
    // Without "files", npm packs whatever happens to be in the staging
    // directory. That is correct today only by accident; state it explicitly.
    val files = Seq("main.js") ++
      (if (hasSourceMap) Seq("main.js.map") else Nil) ++
      (if (hasTypes) Seq("index.d.ts") else Nil) ++
      Seq("README.md")
    sb.append(
      s"""  "files": [${files.map(f => s""""${escapeJson(f)}"""").mkString(", ")}],\n"""
    )
    if (keywords.nonEmpty) {
      val kws = keywords
        .map(k => s""""${escapeJson(k)}"""")
        .mkString(", ")
      sb.append(s"""  "keywords": [$kws],\n""")
    }
    if (author.nonEmpty) {
      sb.append(s"""  "author": "${escapeJson(author)}",\n""")
    }
    sb.append(s"""  "license": "${escapeJson(license)}",\n""")
    sb.append(s"""  "homepage": "https://github.com/$ghOrg/$ghRepo#readme",\n""")
    sb.append(s"""  "bugs": {\n""")
    sb.append(s"""    "url": "https://github.com/$ghOrg/$ghRepo/issues"\n""")
    sb.append(s"""  },\n""")
    // A scoped package is private by default on npmjs; publishing one without
    // this fails with a 402 unless the registry account carries a paid plan.
    if (fullName.startsWith("@")) {
      sb.append(s"""  "publishConfig": {\n""")
      sb.append(s"""    "access": "public"\n""")
      sb.append(s"""  },\n""")
    }
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
