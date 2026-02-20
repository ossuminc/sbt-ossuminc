# sbt-ossuminc Plugin Guide for Claude Code

This file provides specific guidance for working with the sbt-ossuminc plugin.
For general ossuminc organization patterns, see `../CLAUDE.md` (parent
directory).

**IMPORTANT:** For comprehensive usage documentation, configuration options,
and examples, see `README.md` in this directory. This CLAUDE.md provides a
quick reference; the README is the authoritative source.

## Project Overview

SBT plugin providing build infrastructure and configuration helpers for Ossum
Inc. projects. Defines declarative project types and configuration options
used across all Scala projects in the organization.

**Current version: 1.3.5** (released Feb 2026)

## Release Process

Releases are triggered by creating a GitHub Release:

```bash
git tag X.Y.Z && git push origin X.Y.Z
gh release create X.Y.Z --title "vX.Y.Z" --generate-notes
```

The `.github/workflows/release.yml` workflow triggers on release
creation (not tag push) and:
1. Builds and tests with JDK 25 Temurin
2. Publishes to GitHub Packages via `sbt clean test publish`
3. Uploads the plugin JAR to the release as a download artifact

Manual dispatch is also available via `workflow_dispatch` with a
tag input.

## Project Types Provided

- `Root()` - Aggregator project
- `Module()` - Standard module
- `CrossModule()(JVM, JS, Native)` - Cross-platform module
- `Plugin()` - SBT plugin
- `Program()` - Executable program
- `DocSite()` - Documentation site

## Configuration Helpers (`With.*`)

### Parameterless Options

- `With.typical` - Standard Scala 3 setup with publishing
- `With.scala2` / `With.scala3` - Scala version
- `With.publishing` / `With.noPublishing` - Artifact publishing
- `With.coverage(percent)` - Code coverage
- `With.BuildInfo.configure` - BuildInfo plugin (default config)
- `With.git` - Git commands in sbt
- `With.dynver` - Dynamic versioning
- `With.scalajs` - Scala.js with default configuration
- `With.noMiMa` - Disable binary compatibility checking

### Parameterized Options

**`With.ScalaJS(...)`** - Scala.js configuration
- Signature: `(header, hasMain, forProd, withCommonJSModule)(project)`
- Example:
  ```scala
  .jsConfigure(With.ScalaJS(
    header = "RIDDL: module-name",
    hasMain = false,
    forProd = true,
    withCommonJSModule = true
  ))
  ```

**`With.Native(...)`** - Scala Native configuration
- Signature: `(mode, buildTarget, gc, lto, ...)(project)`
- Modes: "debug", "fast", "full", "size", "release"
- Example:
  ```scala
  .nativeConfigure(With.Native(
    mode = "fast",
    buildTarget = "static",
    gc = "none",
    lto = "none"
  ))
  ```

**`With.BuildInfo.withKeys(...)`** - BuildInfo with custom keys
- Signature: `withKeys(key -> value, ...)(project)` (curried)
- Example:
  ```scala
  .jvmConfigure(With.BuildInfo.withKeys(
    "key1" -> value1,
    "key2" -> value2
  ))
  ```

**`With.Akka.forRelease(...)`** - Akka platform dependencies (v1.2.4+)
- See README.md for full parameter documentation
- Core Akka modules are always included
- Optional modules via boolean flags: `withHTTP`, `withGrpc`, `withPersistence`,
  `withProjections`, `withManagement`, `withManagementKubernetes`, `withKafka`,
  `withInsights`
- Example:
  ```scala
  .configure(With.Akka.forRelease(
    "25.10",
    withHTTP = true,
    withPersistence = true,
    withInsights = true
  ))
  ```

**Note for riddl-server-infrastructure dependents:** If your server depends on
`riddl-server-infrastructure`, some Akka modules (core, HTTP) are already
provided transitively. Only use `With.Akka.forRelease()` if you need modules
beyond what the server infrastructure provides (e.g., Kafka, Insights,
Management, Projections).

**`With.Packaging.npm(...)`** - npm package assembly (v1.3.0+)
- For Scala.js projects, assembles output into npm-publishable package
- Signature: `(scope, pkgName, pkgDescription, keywords, esModule,
  templateFile)(project)`
- Tasks: `npmPrepare` (pure sbt), `npmPack` (requires npm on PATH)
- Template mode: set `templateFile` with `VERSION_PLACEHOLDER`
- TypeScript defs auto-discovered from `<module>/types/index.d.ts`

**`With.Publishing.npm(...)`** - npm registry publishing (v1.3.0+)
- Signature: `(registries)(project)` where registries is
  `Seq("npmjs")` and/or `Seq("github")`
- Auth via `NPM_TOKEN` / `GITHUB_TOKEN` env vars

**`With.Packaging.homebrew(...)`** - Homebrew formula generation (v1.3.0+)
- Signature: `(formulaName, binaryName, pkgDescription, homepage,
  javaVersion, tapRepo, variant)(project)`
- Variants: `"universal"` (JVM with openjdk dep) or `"native"`
- Task: `homebrewGenerate` produces `.rb` file with SHA256

**`With.Packaging.linux(...)`** - Native binary tar.gz archive (v1.3.0+)
- Signature: `(pkgName, pkgDescription, arch, os, includeReadme,
  includeLicense)(project)`
- Auto-detects host OS/arch (Scala Native compiles for host only)
- Task: `linuxPackage` produces `<name>-<ver>-<os>-<arch>.tar.gz`

**Other parameterized options:**
- `With.Laminar(...)` - Laminar + DOM dependencies
- `With.MiMa(...)` - Binary compatibility checking
- `With.Packaging.universal(...)` - Universal packaging
- `With.Packaging.dockerDual(...)` - Dev/prod Docker images
- `With.Packaging.windowsMsi(...)` - Placeholder (not yet implemented)
- `With.GithubPublishing` - GitHub Packages publishing

**For all configuration options, examples, and migration notes, refer to
README.md in this directory.**

## Migration from 0.x to 1.0.0

**Breaking changes:**
- `With.Javascript(...)` → `With.ScalaJS(...)`
- `With.Native()` → `With.Native(...)` (now requires parameter list, not just `()`)
- `With.BuildInfo.withKeys(...)` → `With.BuildInfo.withKeys(...)(project)` (curried function)

## Build Commands

```bash
# Compile
sbt compile

# Test
sbt test

# Publish locally
sbt publishLocal

# Format code
sbt scalafmt
```

## Usage in Other Projects

All Scala projects in ossuminc use this plugin. Add to `project/plugins.sbt`:

```scala
addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "1.3.0")
```

### Example CrossModule Definition

```scala
lazy val mymodule_cp = CrossModule("mymodule", "riddl-mymodule")(JVM, JS, Native)
  .dependsOn(cpDep(utils_cp), cpDep(language_cp))
  .configure(With.typical, With.GithubPublishing)
  .settings(
    description := "Description here"
  )
  .jvmConfigure(With.coverage(50))
  .jsConfigure(With.ScalaJS("RIDDL: mymodule", withCommonJSModule = true))
  .nativeConfigure(With.Native(mode = "fast"))

lazy val mymodule = mymodule_cp.jvm
lazy val mymoduleJS = mymodule_cp.js
lazy val mymoduleNative = mymodule_cp.native
```

Then add to root aggregation: `.aggregate(..., mymodule, mymoduleJS, mymoduleNative)`

## Git Workflow

### Version Management
- Uses `sbt-dynver` for dynamic versioning based on git tags
- Tag format: `v1.2.3` creates version `1.2.3`
- Between tags: `1.2.3-N-hash-YYYYMMDD-HHMM` (N commits since tag)

### Commit Messages
```
Short description (imperative mood)

Detailed explanation of what changed and why.

Co-Authored-By: Claude <model> <noreply@anthropic.com>
```

## Related Projects

All ossuminc Scala projects depend on this plugin:
- `../riddl/` - RIDDL compiler (primary user)
- `../synapify/` - Desktop application
- `../riddl-idea-plugin/` - IntelliJ plugin
