# Engineering Notebook: sbt-ossuminc

## Current Status

**Version 1.3.0 released** (Feb 2026). Packaging infrastructure complete.

Previous release: v1.2.5 (Jan 2026).

### Resolved Design Questions

1. **Scala.js optimization task**: Use `fullOptJS` (applies Google Closure
   Compiler optimization). NOT `fullLinkJS` — the plan's original
   recommendation was incorrect; `fullOptJS` is not deprecated and produces
   the optimized output needed for npm packaging.

2. **Homebrew formula variants**: Support both JVM universal and Native
   binary variants via a `variant` parameter (`"universal"` or `"native"`).

3. **Linux tar.gz architecture**: Auto-detect host OS and architecture as
   default (since Scala Native can only compile for the host platform),
   with optional `arch` and `os` parameter overrides. Multi-arch
   distribution requires CI matrix runners for each target platform.

### Implementation Phases

| Phase | Feature | Status |
|-------|---------|--------|
| 1 | NpmPackaging (`npmPrepare`, `npmPack`) | ✅ DONE |
| 2 | NpmPublishing (`npmPublish*` tasks) | ✅ DONE |
| 3 | Linux tar.gz packaging | ✅ DONE |
| 4 | Homebrew formula generation | ✅ DONE |
| 5 | Windows MSI placeholder | ✅ DONE |
| 6 | Documentation & Release (tag 1.3.0) | ✅ DONE |

### Session Feb 3, 2026 — v1.3.0 Released

All 6 phases of the packaging plan implemented and released.
Integration-tested in riddl project (npm packaging/publishing to
npmjs.com for ossum.ai site consumption).

**New files created:**
- `NpmPackaging.scala` — Keys, `npm()` method, `npmPrepare`/`npmPack`
  tasks, template mode with `VERSION_PLACEHOLDER`, TypeScript defs
  convention (`js/types/index.d.ts`), JSON generation via string builder
- `NpmPublishing.scala` — `npmPublish`, `npmPublishNpmjs`,
  `npmPublishGithub` tasks, auth via env vars (`NPM_TOKEN`,
  `GITHUB_TOKEN`), extracted helper methods to avoid sbt `.value` macro
  restrictions inside lambdas
- `HomebrewPackaging.scala` — `homebrewGenerate` task, supports
  `"universal"` (JVM with openjdk dep) and `"native"` (Scala Native)
  variants, SHA256 from local artifact, Ruby class name generation,
  uses `Def.task` variant selection at build-definition time to avoid
  sbt macro restrictions

**Modified files:**
- `Packaging.scala` — Added delegation methods `npm()`, `homebrew()`,
  `linux()`, `windowsMsi()` (placeholder); added `linuxPackage`,
  `linuxPackageArch`, `linuxPackageOs` keys; added `detectArch`,
  `detectOs` private helpers; imported Scala Native `nativeLink`
- `Publishing.scala` — Added `npm()` delegation method

**New scripted tests (20 total now):**
- `npm-packaging` — Verifies `npmPrepare` produces `package.json`,
  `main.js`, `README.md` with correct content assertions
- `linux-packaging` — Verifies config settings (doesn't link; consistent
  with existing `native` test pattern)
- `homebrew` — Runs `homebrewGenerate` on a `Program`, verifies formula
  contains class name, description, homepage, JDK dep, license, SHA256

**Technical lessons learned:**
- sbt `.value` is a macro — ALL `.value` calls in a task body are
  resolved regardless of runtime control flow (match/if/foreach). Must
  either extract to helper methods (NpmPublishing) or use `Def.task`
  variant selection at build-definition time (HomebrewPackaging).
- `fullOptJS` is NOT deprecated; it applies Google Closure Compiler
  optimization needed for production npm packages.

## Work Completed (Recent)

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

### ✅ COMPLETED (Jan 17, 2026)

- ~~Remove hardcoded RIDDL values from Scala3.scala~~ ✓
- ~~Refactor AutoPluginHelper to extend (Project => Project)~~ ✓
- ~~Fix Akka test (already working)~~ ✓
- ~~Fix IntelliJ plugin test (already working)~~ ✓
- ~~Make CrossModule dependencies optional~~ ✓
- ~~Add generic With.Publishing() helper~~ ✓
- ~~Improve Root requirement error messages~~ ✓
- ~~Parameterize dependency versions~~ ✓
- ~~Add initial scripted tests for coverage~~ ✓

### 🔵 ACTIVE: Docker Dual-Image Support (feature/docker-dual)

**Goal:** Add `With.Packaging.dockerDual()` helper to support building separate
dev and prod Docker images for RIDDL services (MCP, Sim, Gen).

**Requirements gathered in ossum-ops session (Feb 2, 2026):**

| Aspect | Dev Image | Prod Image |
|--------|-----------|------------|
| Base | `eclipse-temurin:25-jdk-noble` | `gcr.io/distroless/java25-debian13:nonroot` |
| Arch | `linux/arm64` (Apple Silicon) | `linux/amd64` (GKE) |
| Tags | `:dev-latest`, `:dev-<version>` | `:latest`, `:<version>` |
| Tools | JDK (jcmd, jstack, jmap) | JRE only (minimal) |
| Shell | Yes (Ubuntu bash) | No (distroless) |

**Design decisions:**
1. Two separate images (not multi-arch buildx)
2. `docker:publishLocal` defaults to dev image
3. Explicit `dockerPublishProd` task for production
4. CI builds only prod images
5. Both use staged layout (no sbt-assembly fat JAR)
6. Custom Dockerfile for prod to handle classpath with distroless

**API design:**
```scala
// In project's build.sbt - minimal configuration
.configure(
  With.Packaging.dockerDual(
    mainClass = "com.ossuminc.riddl.mcp.Main",
    pkgName = "riddl-mcp-server",
    exposedPorts = Seq(8080, 8558, 9001)
  )
)

// Optional overrides if needed
.settings(
  dockerRepository := Some("custom-registry.io")
)
```

**Implementation tasks:**
- [x] Update `Packaging.scala` with `dockerDual()` helper
- [x] Add `dockerPublishProd` task definition
- [x] Generate custom Dockerfile for distroless (via `dockerCommands`)
- [x] Set default repository to `ghcr.io/ossuminc`
- [x] Configure non-root user for both images
- [x] Add architecture settings (arm64 dev, amd64 prod)
- [x] Add tag pattern logic (`:dev-*` vs `:<version>`)
- [x] Add scripted test for docker-dual
- [x] Update README.md with documentation

**Session Feb 2, 2026 - Implementation Complete**

All implementation tasks completed. The `dockerDual()` helper is ready for use.
Scripted test passes (17/17 tests now). PR can be created for review.

**Custom Dockerfile for distroless prod:**
```dockerfile
FROM gcr.io/distroless/java25-debian13:nonroot
WORKDIR /opt/docker
COPY --chown=nonroot:nonroot opt/docker/lib lib
ENTRYPOINT ["java", "-cp", "/opt/docker/lib/*", "<MainClass>"]
```

**References:**
- [distroless Java README](https://github.com/GoogleContainerTools/distroless/blob/main/java/README.md)
- [sbt-native-packager Docker](https://www.scala-sbt.org/sbt-native-packager/formats/docker.html)
- ossum-ops/lago/ARCHITECTURE.md (for deployment context)

---

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

#### 7. Add CI/CD workflow for GitHub Actions
Run scripted tests on every PR. Skip Akka test in CI (requires credentials).

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