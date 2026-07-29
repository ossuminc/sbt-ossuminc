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

**Current version: 3.0.3** (released Jul 2026) — **sbt 2.0.2+ / Scala 3**.
The 1.x line (last release **v1.4.0**) remains for sbt 1.x builds.

## Release Process

Releases are triggered by creating a GitHub Release:

```bash
git tag X.Y.Z && git push origin X.Y.Z
gh release create X.Y.Z --title "vX.Y.Z" --generate-notes
```

The `.github/workflows/release.yml` workflow triggers on release
creation (not tag push) and:
1. Builds with JDK 25 Temurin (checkout uses `fetch-tags: true` so dynver
   sees the release tag)
2. Publishes to GitHub Packages via `sbt "; clean; test; publish"`
   (sbt 2 parses CLI args as one command line — commands must be `;`-separated)
3. Uploads the plugin JAR to the release as a download artifact
4. **`notify-blog`** (minor+ only) — dispatches a `repository_dispatch`
   (`event_type: sbt-ossuminc-release`) to `ossuminc/ossum.ai`, which renders
   the release notes into a blog post; deploying that post triggers the
   newsletter send. Gated by `if: endsWith(tag, '.0')` so **patch releases
   never announce**. `continue-on-error` — a dispatch hiccup must not red an
   already-published release. Needs the `BLOG_DISPATCH` secret (present).

**CRITICAL — `release.yml` is the SOLE publisher.** Never run `sbt publish`
locally for a release: GitHub Packages forbids overwrite, so the workflow's
publish then dies with a 409 before attaching the JAR. `publishLocal`
(→ `~/.ivy2/local`) is safe and does not collide.

See `NOTEBOOK.md` → "sbt 2 CI / release gotchas" for the hard-won details
(`;`-separated commands, `fetch-tags`, ThisBuild-scoped dynver settings).

Manual dispatch is also available via `workflow_dispatch` with a
tag input.

### dynver stale-version gotcha (hit twice; costs 10 min if forgotten)

After `git tag -a X.Y.Z`, `sbt 'show version'` may still report the *previous*
version. The culprit is sbt 2's persistent **`sbtn` server**, which computes
dynver once at server startup and holds it — **purging the disk caches does
not help**. Fix: run **`sbt shutdown`**, then re-check. Only if it is still
wrong should you purge `~/Library/Caches/sbt`, `~/.cache/sbt`, and `target`.
Confirm ground truth with `git describe --tags`. Never `pkill -f sbt`.

**As of 3.1.0 the plugin no longer relies on you remembering this.**
`With.DynVer` wires a freshness check into the publish tasks: every publish
logs `Publishing version <ver> (HEAD <sha>)`, and if the resolved version
carries a `-<n>-<sha>` component naming a commit that is not HEAD, `publish`
**fails** while `publishLocal` / `publishM2` **warn**. (Remote publishing is
not correctable in place — GitHub Packages forbids overwriting a coordinate —
whereas a local publish is cheap to redo.)

The check is deliberately conservative: a version sitting exactly on a tag
carries no sha to compare, so it is never flagged. That is what keeps a tagged
CI release from being failed by a check that cannot evaluate it. The pure
decision function is `DynamicVersioning.stalenessOf(version, headSha)`,
asserted by the `publishing` scripted test.

This came from riddl, which published `1.31.0-182-2bc257ae` — two commits
stale — and got `[success]`. Consumers testing that coordinate were running
code that predated the fixes they were asked to verify.

### Release candidates (3.1.0+)

Tag an RC `X.Y.Z-rc.N`. dynver handles it already: `1.32.0-rc.1` yields
version `1.32.0-rc.1` with **`isSnapshot = false`** — correct, since an RC is
a real immutable artifact, and exactly why `isSnapshot` is **not** a proxy for
"is this a prerelease?".

- **`isPrerelease` / `isReleaseCandidate`** — build settings, available
  unqualified in `build.sbt`, so nothing string-matches the version.
- **npm dist-tag is derived from the version**: `-rc.N` → `rc`, any other
  prerelease → `dev`, otherwise `latest`. Without this, publishing an RC moves
  npm's `latest` and a plain `npm install` resolves to the RC. Escape hatch:
  the `npmDistTag` setting (default lives in `projectSettings`, so an override
  wins from anywhere).
- **Not guarded by the plugin:** `gh release create --prerelease` still fires
  `release: [created]`, so Homebrew-tap and announcement jobs must branch on
  `github.event.release.prerelease` or they will overwrite the stable formula
  with an RC. See README → "Release Candidates and Prereleases".

## Project Types Provided

- `Root()` - Aggregator project
- `Module()` - Standard module
- `CrossModule(dir, name, scalaVersion="3.8.4")(JVM, JS, Native)` -
  Cross-platform module (built on sbt 2's built-in `projectMatrix`)
- `Plugin()` - SBT plugin
- `Program()` - Executable program
- `DocSite()` - Documentation site

### `CrossModule` automatic behaviors (don't re-add these in consumers)

`CrossModule.scala`'s `matrix` lazy val applies two things consumers used to
do by hand. Both are covered by the `cross` scripted test:

1. **`.defaultAxes(VirtualAxis.jvm, VirtualAxis.scalaABIVersion(sv))`** —
   pins the JVM/Scala-3 row as the default axis so project IDs stay clean
   (`utils`, `utilsJS`, `utilsNative`; no `3` ABI suffix). Consumers reference
   these IDs in `build.sbt` vals, `.aggregate(...)`, and `addCommandAlias`.
   *Note:* verified to be a **no-op** under sbt 2.0.2 (its built-in
   projectMatrix already defaults the ABI correctly); kept as defensive,
   self-documenting intent. The ABI suffix is a multi-version disambiguation
   and CrossModule declares only one Scala version.
2. **scalajs-stubs auto-add** (3.0.3+) — when the target set contains **JS**,
   `scalajs-stubs % provided` is added to the **JVM and Native** rows so shared
   code using `@JSExport*` (from `scala.scalajs.js.annotation`) compiles there.
   The Native row **forces the JVM artifact**:
   `"org.scala-js" % "scalajs-stubs_3"` (single `%`, hardcoded `_3`) because
   scalajs-stubs is JVM-only and projectMatrix's platform-aware `%%` would
   chase a nonexistent `scalajs-stubs_native0.5_3`. Non-JS modules get nothing.
   Without this, consumers hit `value scalajs is not a member of scala`.

## Configuration Helpers (`With.*`)

### Parameterless Options

- `With.typical` - Standard Scala 3 setup with publishing
- `With.Scala2` / `With.Scala3` - Scala version
- `With.publishing` / `With.noPublishing` - Artifact publishing
- `With.coverage(percent)` - Code coverage
- `With.BuildInfo.configure` - BuildInfo plugin (default config)
- `With.Git` - Git commands in sbt
- `With.DynVer` - Dynamic versioning
- `With.ScalaJS` - Scala.js with default configuration
- `With.noMiMa` - Disable binary compatibility checking
- `With.NoDocs` - Suppress Scaladoc/Javadoc generation (3.0.2+); clears
  `Compile / doc / sources`. The empty `packageDoc` artifact is still
  produced, so Sonatype's javadoc-jar requirement stays satisfied.

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
  templateFile, typesDir)(project)`
- Tasks: `npmPrepare` (pure sbt), `npmPack` (requires npm on PATH)
- Template mode: set `templateFile` with `VERSION_PLACEHOLDER`
- TypeScript defs auto-discovered from `<moduleDir>/js/types/index.d.ts`,
  falling back to `<moduleDir>/types/`; override with `typesDir`
- A `package.json.template` at `<moduleDir>/js/` or `<moduleDir>/` is
  auto-discovered (3.1.0+) and **wins over** the generated document

### `npmTypesDir` must not use `baseDirectory` (3.0.4 fix)

A `projectMatrix` row's `baseDirectory` is a **synthetic**
`.sbt/matrix/<rowId>/` holding no sources, so the old
`baseDirectory / "types"` lookup could never match. It failed **silently**:
`npmTypesDir` was `None`, `index.d.ts` was never staged, and `package.json`
omitted `types`/`exports`. `@ossuminc/riddl-lib` shipped with no TypeScript
definitions at all from riddl's sbt 2 migration until this was found.

The convention now anchors on `sourceDirectory.getParentFile`, which
projectMatrix sets to `<realModuleBase>/src` and which is trivially correct on
plain projects too. Under sbt 1's `CrossProject` the JS row genuinely was based
at `<mod>/js`, which is why the original code was right at the time.

**Any new path convention in a helper must be derived this way, not from
`baseDirectory`** — and must log both outcomes. Covered by the
`npm-crossmodule-types` scripted test, whose build declares no override.

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

All Scala projects in ossuminc use this plugin. **Requires sbt 2.0.2+**
(`sbt.version=2.0.2` in `project/build.properties`) and Scala 3. Add to
`project/plugins.sbt`:

```scala
addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "3.0.3")
```

### Example CrossModule Definition

```scala
lazy val mymodule_cp = CrossModule("mymodule", "riddl-mymodule", scalaVersion = "3.8.4")(JVM, JS, Native)
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
- Tag format: `1.2.3` — **no `v` prefix** (`ThisBuild / dynverVTagPrefix :=
  false`; a `v`-prefixed tag is not matched and yields a `0.0.0+...` version)
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
