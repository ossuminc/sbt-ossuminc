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

**Current version: 1.2.5** (updated Jan 2026)

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

**Other parameterized options:**
- `With.Laminar(...)` - Laminar + DOM dependencies
- `With.MiMa(...)` - Binary compatibility checking
- `With.Packaging.universal(...)` - Universal packaging
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
addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "1.2.5")
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
