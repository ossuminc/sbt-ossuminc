# Engineering Notebook: sbt-ossuminc

## Current Status

Version 1.1.0 released and published to GitHub Packages. Comprehensive
maintenance analysis completed (Jan 2026) identifying 15 UX issues with
prioritized action plan. README completely rewritten with design philosophy
and full helper documentation.

## Work Completed (Recent)

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

### 🔴 CRITICAL

#### 1. Remove hardcoded RIDDL values from Scala3.scala
**File:** `src/main/scala/com/ossuminc/sbt/helpers/Scala3.scala`

The following lines contain RIDDL-specific values that break reusability:
- Line 22: `-project:RIDDL`
- Line 25: `-siteroot:doc/src/hugo/static/apidoc`
- Line 27: `-doc-canonical-base-url:https://riddl.tech/apidoc`
- Line 47: `apiURL := Some(url("https://riddl.tech/apidoc/"))`

**Fix:** Make documentation options parameterizable:
```scala
def configure(
  version: Option[String] = None,
  scala3Options: Seq[String] = Seq.empty,
  projectName: Option[String] = None,
  docSiteRoot: Option[String] = None,
  docBaseURL: Option[String] = None
)(project: Project): Project
```

### ⚠️ HIGH PRIORITY

#### 2. Standardize naming conventions
Migrate lowercase helpers to PascalCase objects with `apply()` methods:
- `With.build_info` → `With.BuildInfo()`
- `With.dynver` → `With.DynVer()`
- `With.git` → `With.Git()`
- `With.header` → `With.Header()`
- `With.java` → `With.Java()`

Keep old names as deprecated aliases for one version.

### 🟡 MEDIUM PRIORITY

#### 3. Fix Akka test (license key required)
**Root cause:** As of October 2024, Akka requires a license key and
repository token.

**Fix:** Update `Akka.scala` to accept optional repository token:
```scala
def forRelease(
  release: String = "",
  repositoryToken: Option[String] = None
)(project: Project): Project = {
  val akkaRepo = repositoryToken match {
    case Some(token) => s"https://repo.akka.io/$token/maven"
    case None => sys.env.get("AKKA_REPO_TOKEN") match {
      case Some(token) => s"https://repo.akka.io/$token/maven"
      case None => "https://repo.akka.io/maven" // Fallback
    }
  }
  // ...
}
```

**Resources:**
- Get free token: https://akka.io/key
- Akka License Docs: https://doc.akka.io/reference/release-notes/

#### 4. Fix IntelliJ plugin test (sbt-idea-plugin 5.x)
**Root cause:** Upgraded from `org.jetbrains % sbt-idea-plugin % 4.1.2` to
`org.jetbrains.scala % sbt-idea-plugin % 5.0.4` - major version with
breaking changes.

**Investigation steps:**
1. Run verbose test:
   `sbt "scripted sbt-ossuminc/idea-plugin" -Dsbt.scripted.log=true`
2. Check sbt-idea-plugin 5.0 changelog on GitHub
3. Verify if `packageArtifactZip` task name changed
4. Update `IdeaPlugin.scala` helper if needed

#### 5. Make CrossModule dependencies optional
**File:** `CrossModule.scala` (lines 59-64)

Currently auto-adds to ALL cross-platform projects:
- `scala-java-time % 2.6.0`
- `scalatest % 3.2.19` (test)

**Fix:** Remove automatic injection, make opt-in:
```scala
CrossModule("foo", "bar")(JVM, JS)
  .configure(With.Scalatest())      // Opt-in for testing
  .configure(With.ScalaJavaTime())  // Opt-in for java.time
```

**Breaking change:** Users must explicitly add dependencies.

#### 6. Add generic `With.Publishing()` helper
**New file:** `helpers/Publishing.scala`

```scala
object Publishing extends AutoPluginHelper {
  def configure(project: Project): Project = {
    val preferGitHub = RootProjectInfo.Keys
      .preferGitHubPublishing.?.value.getOrElse(true)
    if (preferGitHub) GithubPublishing.configure(project)
    else SonatypePublishing.configure(project)
  }
}
```

#### 7. Improve Root requirement error messages
**Files:** `GithubPublishing.scala`, `SonatypePublishing.scala`

Add validation:
```scala
def configure(project: Project): Project = {
  if (RootProjectInfo.Keys.gitHubOrganization.?.value.isEmpty) {
    sys.error(
      "You must define a Root(...) project before using GithubPublishing"
    )
  }
  // ... rest of implementation
}
```

#### 8. Parameterize dependency versions
**Files:** `Javascript.scala`, `Native.scala`, `CrossModule.scala`

Allow overriding hardcoded versions:
```scala
def apply(
  // ... existing params ...
  scalaJavaTimeVersion: String = "2.6.0",
  scalatestVersion: String = "3.2.19"
)(project: Project): Project
```

### 🟢 LOW PRIORITY

#### 9. Remove or implement NodeTarget
**File:** `CrossModule.scala` (lines 21, 76-80)

Dead code - `NodePlatform.enable` does nothing. Either remove entirely or
implement properly.

#### 10. Expose Miscellaneous helpers in With object
**File:** `OssumIncPlugin.scala`

Add to `With` object:
```scala
val ClassPathJar = Miscellaneous.useClassPathJar _
val UnmanagedJars = Miscellaneous.useUnmanagedJarLibs _
val ShellPrompt = Miscellaneous.buildShellPrompt
```

#### 11. Clarify composite helpers
Document what `basic`, `typical`, `everything` include. Consider if
`typical` should include publishing by default.

#### 12. Make Root project ID configurable
**File:** `Root.scala` (line 38)

Currently hardcoded to `"root"`. Add parameter:
```scala
def apply(
  projectId: String = "root",  // New parameter
  ghRepoName: String = "",
  // ... rest
): Project
```

#### 13. Remove placeholder Packaging methods
**File:** `Packaging.scala`

`jdkPackager()`, `linuxDebian()`, `linuxRPM()` do nothing. Either implement
or remove.

#### 14. Remove `project/SonatypePublishing.scala`
No longer used after switching to GitHub Packages.

#### 15. Add CI/CD workflow for GitHub Actions
Run scripted tests on every PR. Skip Akka test in CI (requires credentials).

---

## Test Coverage Status

### Current Test Results (Jan 2026)

| Test Scenario  | Purpose                       | Status                    |
|----------------|-------------------------------|---------------------------|
| basic          | Basic module configuration    | ✅ PASS                   |
| multi          | Multi-module projects         | ✅ PASS                   |
| cross          | Cross-platform (JVM/JS/Native)| ✅ PASS                   |
| native         | Scala Native compilation      | ✅ PASS                   |
| scalajs        | Scala.js compilation          | ✅ PASS                   |
| packaging      | Universal packaging           | ✅ PASS                   |
| program        | Executable programs           | ✅ PASS                   |
| everything     | Full feature set              | ✅ PASS                   |
| scalably-typed | TypeScript facades            | ✅ PASS (fixed)           |
| akka           | Akka dependencies             | ⚠️ FAIL (needs license)   |
| idea-plugin    | IntelliJ plugin development   | ⚠️ FAIL (5.x API change)  |

**Pass rate:** 9/11 (82%)

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

**CrossModule dependency changes:**
```scala
// Old (automatic)
CrossModule("foo", "bar")(JVM, JS)

// New (explicit)
CrossModule("foo", "bar")(JVM, JS)
  .configure(With.Scalatest())      // If you want testing
  .configure(With.ScalaJavaTime())  // If you need java.time
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