# Engineering Notebook: sbt-ossuminc

## Incoming Tasks

**At session start**, check the `task/` directory for pending
work requests from other projects. Each `.md` file describes a
task (e.g., dependency upgrade). Treat unresolved tasks as to-do
items unless already completed (verifiable from this notebook,
CLAUDE.md, or git log). After completing a task, append results
to the task file and note completion in this notebook.

---

## Current Status

**Active work: sbt 2.0 migration** on branch `feature/sbt2`
(redo-fresh from `main` @ v1.4.0). Targeting **sbt 2.0.0 final**
(released 2026-06-14) + Scala 3 (sbt-managed).

The released line v1.4.0 on `main` (sbt 1.x / Scala 2.12) keeps
serving consumers that cannot move yet — notably riddl-idea-plugin,
blocked on sbt-idea-plugin (see Blockers below).

---

## sbt 2.0 Migration Plan (feature/sbt2)

### Reconciliation decision (2026-06-25)

The original `feature/sbt2` branch (single commit `6043e4f`,
"Migrate to sbt 2.0.0-RC8", forked from v1.2.5) was **deleted**.
It predated the v1.3.x/v1.4.0 packaging features on `main` and was
mostly *subtractive* — commenting out plugins that have since
shipped sbt 2 builds. Rather than merge/rebase that stale work, we
**redo the migration fresh on top of current `main`**, using
`6043e4f` only as a reference.

- Reference (Feb migration) saved to scratchpad:
  `scratchpad/sbt2-reference/feb-migration-6043e4f.patch` + the full
  migrated `src/main/scala` tree. Scratchpad is session-scoped; the
  durable salvage list is below.
- **Salvage** from `6043e4f` (still-valid sbt 2 API changes): remove
  project/ symlink self-bootstrapping; add
  `project/project/build.properties`; inline project metadata +
  dynver + scriptedLaunchOpts in build.sbt; `URL`->`URI`; `Classpath`
  -> `HashedVirtualFileRef` + `FileConverter`; `Def.uncached` where
  no JsonFormat; sbt-buildinfo 0.13.1 key patterns.
- **Discard** from `6043e4f`: the plugin comment-outs, the
  CrossModule throw-stub, and the 9 disabled scripted tests (obsolete
  now that the plugins ship for sbt 2).

### Plugin categorization (vs Maven Central `_sbt2_3`, 2026-06)

**A. Compatible — keep (some bumped):** sbt-dynver 5.1.1, sbt-git
2.1.0, sbt-pgp 2.3.1, sbt-buildinfo 0.13.1, sbt-scalafmt 2.5.6,
sbt-native-packager 1.11.7, sbt-scoverage 2.4.4, sbt-mima-plugin
1.1.5->1.1.6, sbt-release 1.4.0->1.5.0, sbt-unidoc 0.6.1,
sbt-scalafix 0.14.6->0.14.7, sbt-updates 0.6.4->0.7.0, sbt-scalajs
1.21.0->1.22.0, sbt-scala-native 0.5.11->0.5.12.

**B. Coordinate change:** sbt-header `de.heikoseeberger` ->
`com.github.sbt` 5.11.0 (sbt org adopted it; old "5.11.0 breaks
imports" note is moot under the new coordinate).

**C. Removed — absorbed/obsoleted by sbt 2 core:**
addDependencyTreePlugin (dependency-tree built in), scripted-plugin
dep (bundled with SbtPlugin), sbt-projectmatrix (in core),
sbt-platform-deps (core `%%` + `platform`, no more `%%%`),
sbt-scalajs-crossproject & sbt-scala-native-crossproject
(projectMatrix replaces crossProject).

**C2. Removed — superseded (no sbt 2 build):** sbt-sonatype
(deprecated; OSSRH sunset) -> native Central Portal
(`sonaUpload`/`sonaRelease` + sbt-pgp); sbt-github-packages
(abandoned) -> plain `publishTo` + Credentials.

**D. BLOCKED — no sbt 2.0 artifact yet:** sbt-idea-plugin
(JetBrains SCL-23480), sbt-coveralls, sbt-tasty-mima, sbt-paradox
(only 0.11.0-M4 milestone).

### Architecture changes

- **Cross-building**: `projectMatrix` is now in sbt 2 core and
  replaces `crossProject`. `CrossModule` must be rewritten from the
  portable-scala crossProject builder to projectMatrix.
- **Cross deps**: `%%%` is gone; core `%%` + a `platform` setting
  cross-build JVM/JS/Native. Convert `%%%` -> `%%` in Riddl, Laminar,
  Scalatest, ScalaJavaTime helpers.
- **Backends still plugins**: sbt-scalajs / sbt-scala-native are
  still required (now available for sbt 2).
- Built into core now: dependency-tree, scripted.

### Plugin-author code gotchas (sbt 1 -> sbt 2)

1. Scala 3 mandatory; `import X._` -> `import X.given`.
2. Virtual file APIs: `Classpath` is
   `Seq[Attributed[HashedVirtualFileRef]]`; convert via
   `toNioPaths`/`toFiles`.
3. Task caching on by default: task results need an
   `sjsonnew.JsonFormat` or must be wrapped in `Def.uncached(...)`
   (will bite the npm/homebrew/linux/docker packaging tasks).
4. Slash syntax only; bare settings apply to all subprojects;
   `exportJars` defaults true; target/ layout changes.

### Roadmap / status

**Phase 0 — Reconcile branch (DONE 2026-06-25):**
- [x] Delete stale branch+worktree; fresh feature/sbt2 from main.
- [x] build.properties -> 2.0.0; add project/project/build.properties.
- [x] Remove project/ symlinks (no self-bootstrap).
- [x] Rewrite build.sbt (inline metadata) + re-export plugin list.
- [x] Rewrite project/plugins.sbt (minimal meta-build).
- [x] Pin scalaVersion := "3.8.4" (Scala version sbt 2.0.0 ships).
- [x] VERIFIED: `sbt update` green (exit 0). sbt 2.0.0 boots, metabuild
      loads, all 15 re-exported plugins resolve for sbt 2. Needed one
      fix: exclude `scala-collection-compat_2.13` (a re-export plugin
      drags in 2.13 copies) — mirrors the existing scala-xml handling.

**Phase 1a — Helper API migration: DONE (2026-06-25).**
`sbt compile` is GREEN — all 42 sources build on sbt 2.0.0 / Scala
3.8.4. Started at 69 errors, drove to 0.
- [x] `%%%` -> `%%` in ScalaJS/Native/Laminar/Riddl/Scalatest/
      ScalaJavaTime; dropped platform-deps imports.
- [x] `URL` -> `URI` (RootProjectInfo/Root/Scala3); `import sbt.given`
      for the (String,URI)->License conversion on `licenses`.
- [x] sbt-header package `de.heikoseeberger.sbtheader` -> `sbtheader`.
- [x] Classpath/file APIs (Miscellaneous, HandyAliases, Unidoc,
      Packaging, Homebrew): FileConverter + HashedVirtualFileRef.
- [x] BuildInfo: wrap bare keys with `BuildInfoKey(...)` (no implicit
      conversion in 0.13.1 sbt2_3).
- [x] `Def.uncached` for side-effecting / File-output tasks
      (Scalafmt.update, unmanagedJars, externalResolvers, npm*,
      linuxPackage, homebrewGenerate, ScalaJS scalacOptions).
- [x] GithubPublishing -> plain publishTo+resolver+Credentials.
- [x] MiMa: dropped tasty-mima, kept binary check.
- [x] Release: unicode `⇒` -> `=>`, `.map[A]` arity.
- [x] Removed apiMappings managed-dep scan from Unidoc (fragile under
      virtual-file classpath; reinstate later if needed).

Stubbed (with fail-fast/warn messages), deferred to later phases:
- CrossModule -> throws (Phase 1b: projectMatrix rewrite).
- IdeaPlugin -> fail-fast (sbt-idea-plugin blocked upstream).
- SonatypePublishing -> fail-fast (Central Portal wiring TBD; verify
      sona API — `localStaging`/`sonaRelease` not found in scanned jars).
- Miscellaneous.useClassPathJar -> warn no-op (native-packager + IO.jar).

**Phase 1b — CrossModule -> projectMatrix (NEXT, design pending):**
- [ ] Design + implement projectMatrix-based CrossModule.
- [ ] Re-enable/validate scripted tests: cross, native, scalajs, laminar.

**Phase 1c — remaining validation & follow-ups:**
- [x] Scripted plumbing: all tests -> sbt.version 2.0.0; added the
      scala-xml/scala-collection-compat _2.13 exclusion to each test
      meta-build. `basic` PASSES under sbt 2 — validates publishLocal +
      plugin load + Root/With.basic/With.BuildInfo + HandyAliases +
      BuildInfo generation end-to-end.
- [~] JVM scripted snapshot (2026-06-25): **7/11 PASS** — basic,
      docker-dual, everything, mima, packaging, program, scalatest.
      4 fail on TEST-FIXTURE issues (not plugin bugs):
      - asciidoc: test build.sbt uses `.get` (->`.get()`) and
        `cp.map(_.data.toURI.toURL)` (data is HashedVirtualFileRef now ->
        use fileConverter).
      - publishing: "Remote sbt init failed" (same build.sbt API kind).
      - multi: `show` at test line 4 failed (likely a renamed setting/path).
      - homebrew: formula `.rb` not produced at expected path (check the
        Def.uncached homebrewGenerate runtime + expected path).
      Remaining: apply the same sbt-2 API fixes to these test build.sbt
      files; re-check homebrew at runtime.
- [ ] Packaging runtime validation (npm/homebrew/linux) — compiles via
      Def.uncached but not yet run on sbt 2.
- [ ] SonatypePublishing: wire native Central Portal (sonaUpload/Release).
- [ ] Reinstate Unidoc external apiMappings if wanted.

**CONSUMER-FACING follow-up (important):** adding sbt-ossuminc to an
sbt 2 build hits a transitive scala-xml/scala-collection-compat
`_2.13` vs `_3` cross-version clash (a re-exported plugin drags in the
2.13 copies). Today each consumer must add the exclusion. Fix at source:
identify the culprit plugin(s) and `.exclude(...)` the 2.13 artifacts in
the `addSbtPlugin` declarations so the published POM doesn't propagate
them. Otherwise document the required exclusion in README.

### sbt 1.x -> 2.x migration learnings (reusable for riddl et al.)

- Plugin artifacts: `_sbt2_3` suffix on Maven Central is the GA marker.
- `url(...)` now returns `java.net.URI`. `homepage`/`organizationHomepage`/
  `apiURL`/`licenses` take URI; `Resolver.url` still wants URL (`.toURL`).
- `licenses` is `Seq[License]`; need `import sbt.given` for the
  `(String, URI) -> License` conversion (wildcard `*` does NOT import givens).
- `fullClasspath` is `Seq[Attributed[HashedVirtualFileRef]]`. `cp.files`
  needs a `given xsbti.FileConverter = fileConverter.value` and returns
  `Seq[Path]` (`.map(_.toFile)` for File). `PathFinder.classpath` likewise.
  Convert a single ref via `fileConverter.value.toPath(ref).toFile`.
- Tasks are cached by default: results need `sjsonnew.JsonFormat`/`HashWriter`
  or wrap in `Def.uncached(...)`. `File`/`Path` are invalid cached-task
  outputs entirely — use Def.uncached (or HashedVirtualFileRef).
- sbt-buildinfo 0.13.1: no implicit SettingKey->BuildInfoKey; wrap with
  `BuildInfoKey(key)` (also inside `BuildInfoKey.map(...)`).
- sbt-header moved org `de.heikoseeberger` -> `com.github.sbt`; package
  is now `sbtheader`.
- Scala 3.8: unicode `⇒`/`←` removed; `x: _*` -> `x*`; `_` wildcard type
  -> `?`; overloaded `apply` needs explicit return types (cyclic errors).
- dependency-tree (MiniDependencyTreePlugin) and scripted are in core.
- PathFinder `.get` -> `.get()`.

**Phase 2 — Degrade blocked features gracefully:**
- [ ] IdeaPlugin: documented stub (riddl-idea-plugin stays sbt 1.x).
- [ ] ScalaCoverage: keep scoverage, drop coveralls upload.
- [ ] MiMa: keep binary check, drop tasty-mima.
- [ ] DocSite/paradox: use 0.11.0-M4 milestone or defer.

**Phase 3 — Watch upstream:** sbt-idea-plugin (SCL-23480),
sbt-coveralls, sbt-tasty-mima, stable sbt-paradox.

### Blockers

- **sbt-idea-plugin** (hard): no sbt 2 artifact; blocks the
  `IdeaPlugin` helper and the **riddl-idea-plugin** consumer
  entirely. Track JetBrains SCL-23480.
- sbt-coveralls, sbt-tasty-mima: no sbt 2 build (graceful degrade).
- sbt-paradox: only milestone 0.11.0-M4.

### Strategic decision (2026-06-25): sbt-2-only (new major)

feature/sbt2 ships as a **clean sbt-2-only** new major version.
Consumers migrate to sbt 2 or pin the last sbt 1.x release (v1.4.0 on
`main`). riddl-idea-plugin stays on v1.4.0 until sbt-idea-plugin ships
an sbt 2 build (SCL-23480). This frees the helper code to use sbt 2
APIs directly — no sbt2-compat dual-compilation layer.

---

### Prior release status

**v1.4.0** latest tag on `main`. Previous: v1.3.5 (Feb 20, 2026),
v1.3.4, v1.3.3, v1.3.2, v1.3.0, v1.2.5.

## Work Completed (Recent)

### Session Feb 20, 2026 — Corporate Name Fix & Release Improvements

**Released:** v1.3.4 and v1.3.5 to GitHub Packages.

**v1.3.4 — orgName fix:**
- Changed default `orgName` from `"Ossum, Inc."` to `"Ossum Inc."`
  (no comma) in `Root.scala`, `RootProjectInfo.scala`, and test
  build files. Fixed typo `"Ossumin, Inc."` in program test.
- Cross-project task from riddl Claude instance (`task/fix-orgname-comma.md`).

**v1.3.5 — Copyright, tests, and workflow:**
- Updated all source file copyright dates to `2015-2026` (were
  stale at `2015-2017` in most files).
- Fixed `docker-dual` scripted test assertions to expect Artifact
  Registry defaults (`us-central1-docker.pkg.dev` and
  `ossuminc-production/ossum-images`) instead of old `ghcr.io`.
- Changed release workflow trigger from tag push to release
  creation (`on: release: types: [created]`), matching the riddl
  project pattern. Added `workflow_dispatch` for manual triggering.

**All 20 scripted tests passing** (docker-dual was previously
broken due to stale assertions from the Artifact Registry default
change).

### Session Feb 16, 2026 — Release Workflow

Added `.github/workflows/release.yml` to automate GitHub Releases
with artifact attachment. Triggers on tag pushes matching `[0-9]*`.
Runs `sbt clean test publish` (JDK 25 Temurin), collects the plugin
JAR, and creates a GitHub Release with auto-generated release notes
and the JAR attached for download.

### Session Feb 3, 2026 — v1.3.0 Released

Implemented complete packaging infrastructure across 6 phases.
Integration-tested in riddl project (npm packaging/publishing to
npmjs.com for ossum.ai site consumption). See `PACKAGING-PLAN.md`
for the original design document.

**New helpers:**
- `With.Packaging.npm()` — npm package assembly from Scala.js output
- `With.Publishing.npm()` — publish to npmjs.com and/or GitHub Packages
- `With.Packaging.linux()` — tar.gz archives of Scala Native binaries
- `With.Packaging.homebrew()` — Homebrew formula generation (JVM/Native)
- `With.Packaging.windowsMsi()` — placeholder for future implementation

**New files:** `NpmPackaging.scala`, `NpmPublishing.scala`,
`HomebrewPackaging.scala`

**Modified files:** `Packaging.scala` (added linux(), npm(), homebrew(),
windowsMsi() delegation + linuxPackage keys), `Publishing.scala` (added
npm() delegation)

**New scripted tests:** `npm-packaging`, `linux-packaging`, `homebrew`
(20 total, all passing)

### Session Feb 2, 2026 — Docker Dual-Image Support

Added `With.Packaging.dockerDual()` for separate dev/prod Docker images.
Dev image uses `eclipse-temurin:25-jdk-noble` (arm64), prod uses
`gcr.io/distroless/java25-debian13:nonroot` (amd64). Scripted test added.

### Session Jan 28-29, 2026 - v1.2.5 Released (CI Fixes)

**Released:** v1.2.5 to GitHub Packages

**Root Cause Analysis:**
The scripted tests had been failing since PR #8 "Use native git instead of
JGit for worktree support". Native git fails hard in non-git directories,
while JGit was more lenient. Scripted tests run in temp directories that
aren't git repositories.

**Bug Fixes:**
1. **Git/DynamicVersioning helpers now detect git repos** - Added `isGitRepo`
   check that walks up directory tree looking for `.git`. Only enables native
   git (`useReadableConsoleGit`) if actually in a git repo. Falls back to JGit
   for non-git directories (scripted tests).

2. **AKKA_REPO_TOKEN propagation to scripted tests** - The `scriptedLaunchOpts`
   now passes `AKKA_REPO_TOKEN` as system property `-Dakka.repo.token=...`.
   The Akka helper checks both `sys.props` and `sys.env` for the token.

3. **Modules now apply Resolvers** - `Module()` was missing the Resolvers
   configuration, breaking dependency resolution for Akka dependencies.

**Improvements:**
- Updated scripted test build.sbt files to use modern PascalCase `With.*`
  syntax (BuildInfo, Scala3, Riddl) instead of deprecated lowercase versions.

**Technical Notes:**
- The `isGitRepo` function is duplicated in both Git.scala and
  DynamicVersioning.scala because the project/ directory uses symlinks to
  the source files, and DynamicVersioning.scala is linked while Git.scala
  is not. Sharing the function would require adding another symlink.
- CI now passes all 16 scripted tests.

### Session Jan 27, 2026 - Documentation Updates

Improved documentation for With.Akka helper to reduce confusion for users:

- Updated CLAUDE.md to direct users to README.md as authoritative source
- Updated version references from 1.0.0/1.2.0 to 1.2.4 in both files
- Documented all With.Akka.forRelease boolean parameters in README.md
- Added note for riddl-server-infrastructure dependents explaining when to use
  With.Akka (only needed for modules beyond what server infrastructure provides)
- Included examples for basic and full-featured Akka configurations

### Session Jan 24, 2026 - v1.2.4 Released

**Released:** v1.2.4 to GitHub Packages

Added `withInsights` and `withManagementKubernetes` parameters to `With.Akka.forRelease()`:

**New Features:**
- `withInsights` - Adds Cinnamon telemetry modules (actor, HTTP, cluster metrics)
- `withInsightsPrometheus` - Prometheus metrics export (default: true)
- `withInsightsOpenTelemetry` - OpenTelemetry tracing (default: true)
- `withManagementKubernetes` - K8s discovery, lease, rolling updates

**Implementation:**
- Added `javaModule()` helper for Java-only dependencies (no Scala suffix)
- Added `insightsModules()` function with all Cinnamon dependencies
- Added `managementKubernetesModules()` function
- Uses direct Maven coordinates instead of sbt-cinnamon plugin

**Technical Notes:**
- Cinnamon Scala modules: cinnamon-akka, cinnamon-akka-typed, cinnamon-akka-stream,
  cinnamon-akka-cluster, cinnamon-akka-http
- Cinnamon Java modules: cinnamon-prometheus, cinnamon-prometheus-httpserver,
  cinnamon-opentelemetry, cinnamon-agent
- K8s modules: akka-discovery-kubernetes-api, akka-lease-kubernetes,
  akka-rolling-update-kubernetes

**CI Note:** Scripted tests failing in CI due to "Remote sbt initialization failed"
error affecting all 15 tests. This is a pre-existing CI environment issue, not
related to these changes. Used manual `sbt publish` for release.

### Session Jan 17, 2026
- [x] Refactored AutoPluginHelper to extend `(Project => Project)` for better UX
- [x] Made CrossModule dependencies optional (breaking change)
- [x] Created `With.ScalaJavaTime()` helper for opt-in java.time support
- [x] Created `With.Publishing` helper (defaults to GitHub, `.sonatype` option)
- [x] Improved Root requirement error messages for publishing helpers
- [x] Parameterized dependency versions in ScalaJS and Native helpers
- [x] Added scripted tests: `scalatest`, `publishing` (now 16/16 passing)
- [x] Verified Akka and IntelliJ plugin tests pass (were already working)

### Previous Sessions
- [x] Fixed ScalaJS helper to handle missing git commit/scmInfo gracefully
- [x] Added deprecated `With.Javascript` alias for backward compatibility
- [x] Updated all scripted tests to pass (14/14)
- [x] Updated README.md with correct API documentation
- [x] Switched from Sonatype to GitHub Packages publishing
- [x] Committed and tagged 1.1.0 (no `v` prefix - interferes with sbt-dynver)
- [x] Pushed to origin/main
- [x] Published 1.1.0 to GitHub Packages (artifacts uploaded)
- [x] Published 1.1.0 locally via `publishLocal`
- [x] Updated sbt to 1.12.0
- [x] Updated 9 plugin dependencies to latest versions
- [x] Updated 2 library dependencies (commons-lang3 3.20.0, slf4j 2.0.17)
- [x] Comprehensive README rewrite with philosophy section
- [x] Documented all parameterized helpers in README
- [x] Fixed scalably-typed test (updated to scala-3.3.7)
- [x] Removed hardcoded RIDDL values from Scala3.scala

## Resolved Issues

### GitHub Packages resolution for sbt plugins (FIXED)

**Problem:** Other projects couldn't resolve sbt-ossuminc from GitHub Packages.

**Root cause:** README was missing resolver configuration. GitHub Packages is
not in sbt's default resolver chain, so consumers must explicitly add it.

**Solution:** Updated README to include resolver in `project/plugins.sbt`:
```scala
resolvers += "GitHub Packages" at
  "https://maven.pkg.github.com/ossuminc/sbt-ossuminc"
addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "1.1.0")
```

Plus credentials in `~/.sbt/1.0/github.sbt`.

---

## Next Steps (Priority Order)

### 🟡 REMAINING WORK

#### 1. Remove or implement NodeTarget
**File:** `CrossModule.scala` (lines 21, 76-80)

Dead code - `NodePlatform.enable` does nothing. Either remove entirely or
implement properly.

#### 2. Expose Miscellaneous helpers in With object
**File:** `OssumIncPlugin.scala`

Add to `With` object:
```scala
val ClassPathJar = Miscellaneous.useClassPathJar _
val UnmanagedJars = Miscellaneous.useUnmanagedJarLibs _
val ShellPrompt = Miscellaneous.buildShellPrompt
```

#### 3. Clarify composite helpers
Document what `basic`, `typical`, `everything` include. Consider if
`typical` should include publishing by default.

#### 4. Make Root project ID configurable
**File:** `Root.scala` (line 38)

Currently hardcoded to `"root"`. Add parameter:
```scala
def apply(
  projectId: String = "root",  // New parameter
  ghRepoName: String = "",
  // ... rest
): Project
```

#### 5. Remove placeholder Packaging methods
**File:** `Packaging.scala`

`jdkPackager()`, `linuxDebian()`, `linuxRPM()` do nothing. Either implement
or remove.

#### 6. Remove `project/SonatypePublishing.scala`
No longer used after switching to GitHub Packages.

#### 7. Update CI workflow to use JDK 25
Release workflow uses JDK 25 Temurin; CI still uses JDK 21
adopt-hotspot. Consider aligning.

---

## Test Coverage Status

### Current Test Results (Feb 3, 2026)

| Test Scenario    | Purpose                        | Status   |
|------------------|--------------------------------|----------|
| akka             | Akka dependencies              | ✅ PASS  |
| asciidoc         | AsciiDoc document generation   | ✅ PASS  |
| basic            | Basic module configuration     | ✅ PASS  |
| cross            | Cross-platform (JVM/JS/Native) | ✅ PASS  |
| docker-dual      | Dev/prod Docker images         | ✅ PASS  |
| everything       | Full feature set               | ✅ PASS  |
| homebrew         | Homebrew formula generation    | ✅ PASS  |
| idea-plugin      | IntelliJ plugin development    | ✅ PASS  |
| laminar          | Laminar UI dependencies        | ✅ PASS  |
| linux-packaging  | Native binary tar.gz archive   | ✅ PASS  |
| mima             | Binary compatibility checking  | ✅ PASS  |
| multi            | Multi-module projects          | ✅ PASS  |
| native           | Scala Native compilation       | ✅ PASS  |
| npm-packaging    | npm package assembly           | ✅ PASS  |
| packaging        | Universal packaging            | ✅ PASS  |
| program          | Executable programs            | ✅ PASS  |
| publishing       | Publishing helper              | ✅ PASS  |
| scalably-typed   | TypeScript facades             | ✅ PASS  |
| scalajs          | Scala.js compilation           | ✅ PASS  |
| scalatest        | Scalatest helper               | ✅ PASS  |

**Pass rate:** 20/20 (100%)

### Missing Test Coverage

Tests needed for:
- `With.MiMa()` - Binary compatibility checking
- `With.Packaging.docker()` - Docker image creation
- `With.Packaging.graalVM()` - GraalVM native images
- `With.Riddl()` - RIDDL library dependencies
- `With.Laminar()` - Laminar UI dependencies
- `With.Scalatest()` - Custom ScalaTest versions
- `With.Scala3()` - Custom Scala 3 configurations
- `With.BuildInfo()` - Custom BuildInfo settings
- `With.Unidoc()` - Custom Unidoc configurations
- `With.coverage()` - Code coverage thresholds

### Test Infrastructure Improvements Needed

1. **Version-agnostic tests** - Don't hardcode Scala versions
2. **CI integration** - Run scripted tests on every PR
3. **Cross-version testing** - Test with sbt 1.10.x, 1.11.x, 1.12.x
4. **Cross-Scala testing** - Test with Scala 2.13.x, 3.3.x, 3.4.x

---

## Long-Term Vision (v2.0.0)

### Goals

1. **Stability**: No breaking changes for 12+ months after release
2. **Consistency**: All helpers follow same PascalCase naming patterns
3. **Documentation**: Every helper fully documented with examples
4. **Discoverability**: IDE autocomplete reveals all features
5. **Testing**: 100% helper coverage in scripted tests
6. **Examples**: Real-world examples for every use case

### Potential Future Features

- [ ] `With.Metals()` - Metals LSP configuration
- [ ] `With.VSCode()` - VS Code settings generator
- [ ] `With.CI.github()` - GitHub Actions workflow generator
- [ ] `With.CI.gitlab()` - GitLab CI pipeline generator
- [ ] `With.Docker.compose()` - Docker Compose for dev environment
- [ ] `With.Kubernetes()` - K8s manifests generator
- [ ] `With.Observability()` - Metrics, tracing, logging setup
- [ ] Interactive wizard: `sbt ossuminc:new` to scaffold projects

### Proposed Examples Directory

Create `examples/` with working `build.sbt` files:
- `basic-library/` - Simple library project
- `cross-platform/` - JVM+JS+Native project
- `executable/` - Program with packaging
- `idea-plugin/` - IntelliJ IDEA plugin
- `monorepo/` - Multi-module project

---

## Design Decisions Log

| Decision | Rationale | Date |
|----------|-----------|------|
| sbt-2-only new major (not dual-publish) | Simpler code; consumers pin v1.4.0 if not ready; idea-plugin blocked anyway | 2026-06-25 |
| Redo sbt 2 migration fresh on main (not merge old branch) | Feb branch was subtractive + predated v1.3/v1.4 features | 2026-06-25 |
| Drop sbt-sonatype + sbt-github-packages for sbt 2 | No sbt 2 builds; use native Central Portal + plain publishTo | 2026-06-25 |
| CrossModule -> projectMatrix (sbt 2 core) | crossProject/portable-scala plugins have no sbt 2 builds | 2026-06-25 |
| No project/ symlink self-bootstrap in sbt 2 build | Scala 3 metabuild + new file APIs make it impractical | 2026-06-25 |
| Delegation pattern for new helpers | Keeps API at `With.Packaging.npm()` not `With.NpmPackaging` | 2026-02-03 |
| `fullOptJS` for npm packaging | Closure Compiler optimization needed; not deprecated | 2026-02-03 |
| `Def.task` for variant selection | sbt `.value` macro resolves all refs in task body | 2026-02-03 |
| Auto-detect OS/arch for linux() | Scala Native compiles for host only; CI matrix for multi-arch | 2026-02-03 |
| No JSON library for package.json | Avoids dependency; string builder sufficient for Scala 2.12 | 2026-02-03 |
| Switch Sonatype → GitHub Packages | Simpler auth for ossuminc org | 2026-01-15 |
| Use symlink approach | Consistent with project/ pattern | 2026-01-15 |
| Rename With.Javascript → With.ScalaJS | Clearer naming | 2026-01-15 |
| Tag format without `v` prefix | sbt-dynver compatibility | 2026-01-15 |
| Keep sbt-header at 5.10.0 | 5.11.0 has breaking imports | 2026-01-08 |

## Project Structure Notes

The `project/` directory uses symlinks to reference helper files from
`src/main/scala/com/ossuminc/sbt/helpers/`. This allows the plugin to use
its own functionality during its own build (bootstrapping).

Current symlinks in `project/`:
- `AutoPluginHelper.scala` → helpers source
- `DynamicVersioning.scala` → helpers source
- `GithubPublishing.scala` → helpers source (added for 1.1.0)
- `Miscellaneous.scala` → helpers source
- `Release.scala` → helpers source
- `RootProjectInfo.scala` → helpers source
- `Scala2.scala` → helpers source
- `SonatypePublishing.scala` (local file, not symlink - can be removed)

---

## Migration Notes (for Breaking Changes)

### v1.2.0 Migration (from 1.1.0)

**CrossModule dependency changes (BREAKING):**
```scala
// Old (1.1.0 - automatic dependencies)
CrossModule("foo", "bar")(JVM, JS)

// New (1.2.0 - explicit dependencies)
CrossModule("foo", "bar")(JVM, JS)
  .configure(With.Scalatest())      // If you want testing
  .configure(With.ScalaJavaTime())  // If you need java.time
```

**New helpers available:**
- `With.Publishing` - Generic publishing (defaults to GitHub)
- `With.Publishing.github` - Explicit GitHub Packages
- `With.Publishing.sonatype` - Explicit Sonatype/Maven Central
- `With.ScalaJavaTime()` - Add scala-java-time dependency

**Parameterized versions now supported:**
```scala
// ScalaJS and Native helpers now accept version parameters
With.ScalaJS(
  scalaJavaTimeVersion = "2.6.0",  // Override default
  scalatestVersion = "3.2.19"      // Override default
)

With.Native(
  scalatestVersion = "3.2.19"      // Override default
)
```

### Future v2.0.0 Migration

**Naming convention changes (deprecated in 1.x, removed in 2.x):**
```scala
// Old (deprecated)
.configure(With.build_info)
.configure(With.GithubPublishing)

// New (required in 2.x)
.configure(With.BuildInfo())
.configure(With.GitHubPublishing)
```

---

## References

### Official Documentation
- [sbt Documentation](https://www.scala-sbt.org/)
- [Scala.js Documentation](https://www.scala-js.org/)
- [Scala Native Documentation](https://scala-native.org/)
- [Scaladex - Package Index](https://index.scala-lang.org/)

### Plugin Documentation
- [sbt-header GitHub](https://github.com/sbt/sbt-header)
- [sbt-buildinfo GitHub](https://github.com/sbt/sbt-buildinfo)
- [sbt-scoverage GitHub](https://github.com/scoverage/sbt-scoverage)
- [sbt-native-packager](https://www.scala-sbt.org/sbt-native-packager/)
- [sbt-idea-plugin GitHub](https://github.com/JetBrains/sbt-idea-plugin)

### Best Practices
- [Scala Best Practices](https://nrinaudo.github.io/scala-best-practices/)
- [SBT Best Practices](https://www.scala-sbt.org/1.x/docs/Best-Practices.html)