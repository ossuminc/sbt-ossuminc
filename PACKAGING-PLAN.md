# Packaging Infrastructure Plan for sbt-ossuminc

**Target Version**: 1.3.0
**Author**: Claude (AI Assistant)
**Date**: February 3, 2026
**Status**: Design Document — Ready for Implementation

---

## Overview

This document specifies new `With.Packaging.*` and `With.Publishing.*`
helpers for sbt-ossuminc that standardize packaging operations currently
done with hand-rolled scripts in the riddl repository. The goal is to
make npm packaging, Homebrew formula generation, and Linux binary
distribution first-class sbt operations.

### Motivation

The riddl project currently uses:
- `scripts/pack-npm-modules.sh` (226 lines of bash) for npm packaging
- Manual `Dockerfile` and GitHub Actions workflows for Docker
- Manual Homebrew formula in `ossuminc/homebrew-tap`
- No Linux binary distribution support

These should be declarative sbt configurations, consistent with the
existing `With.Packaging.universal()` and `With.Packaging.docker()`
patterns.

### Design Principles

1. **Follow existing patterns** — All helpers extend
   `AutoPluginHelper` as `Project => Project` functions
2. **Separate packaging from publishing** — Assembly/generation is
   pure sbt; publishing shells out to external tools
3. **Convention over configuration** — Sensible defaults from Root
   project settings (org, license, repo URL)
4. **Template mode** — Support `VERSION_PLACEHOLDER` in templates
   for version substitution

---

## API Design

### With.Packaging.npm(...)

Assembles a Scala.js output directory into an npm-publishable package.

```scala
// In build.sbt
.jsConfigure(
  With.Packaging.npm(
    scope = "@ossuminc",
    pkgName = "riddl-lib",
    pkgDescription = "RIDDL Language Library",
    keywords = Seq("riddl", "ddd", "parser", "typescript"),
    esModule = true
  )
)
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `scope` | `String` | `""` | npm scope (e.g. `"@ossuminc"`) |
| `pkgName` | `String` | *required* | npm package name |
| `pkgDescription` | `String` | `""` | Package description |
| `keywords` | `Seq[String]` | `Seq.empty` | npm keywords |
| `esModule` | `Boolean` | `true` | Set `"type": "module"` |
| `templateFile` | `Option[File]` | `None` | Custom package.json template |

**New sbt tasks/keys:**

```scala
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
}
```

**Behavior of `npmPrepare`:**

1. Runs `fullLinkJS` to produce optimized JS output
2. Creates output directory at
   `<module>/js/target/npm-package/`
3. Copies `main.js` and `main.js.map` from Scala.js output
4. Generates or processes `package.json`:
   - If `npmTemplateFile` is set: reads template, replaces
     `VERSION_PLACEHOLDER` with `version.value`
   - Otherwise: generates from settings (scope, name, description,
     keywords, license, repo URL from Root settings)
5. If `npmTypesDir` is set and contains `index.d.ts`: copies it and
   adds `"types"`, `"exports"` fields to package.json
6. Generates minimal README.md with package name and install command
7. Returns the output directory

**Behavior of `npmPack`:**

1. Depends on `npmPrepare`
2. Runs `npm pack --pack-destination=<target>/npm-packages/`
3. Returns the `.tgz` file path

**Convention for TypeScript definitions:**

- Default location: `<module>/js/types/index.d.ts`
- If the file exists, `npmPrepare` copies it automatically
- Future: auto-generation from `@JSExport` annotations (aspirational,
  not in scope for 1.3.0)

### With.Publishing.npm(...)

Publishes a prepared npm package to a registry.

```scala
// In build.sbt
.jsConfigure(
  With.Publishing.npm(
    registries = Seq("npmjs", "github")
  )
)
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `registries` | `Seq[String]` | `Seq("npmjs")` | Target registries |

**Supported registries:**
- `"npmjs"` — registry.npmjs.org (uses `NPM_TOKEN` env var)
- `"github"` — npm.pkg.github.com (uses `GITHUB_TOKEN` env var)

**New sbt tasks:**

```scala
val npmPublish = taskKey[Unit](
  "Publish npm package to configured registries"
)
val npmPublishNpmjs = taskKey[Unit](
  "Publish npm package to npmjs.com"
)
val npmPublishGithub = taskKey[Unit](
  "Publish npm package to GitHub Packages"
)
```

**Behavior:**

1. Depends on `npmPack`
2. For each registry:
   - Sets auth token from environment variable
   - Sets registry URL via `npm config`
   - Runs `npm publish --access public`

### With.Packaging.homebrew(...)

Generates a Homebrew formula `.rb` file for a native or universal
binary.

```scala
// In build.sbt
.jvmConfigure(
  With.Packaging.homebrew(
    formulaName = "riddlc",
    binaryName = "riddlc",
    pkgDescription = "Compiler for the RIDDL language",
    homepage = "https://ossum.tech/riddl/",
    javaVersion = "21"
  )
)
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `formulaName` | `String` | *required* | Formula name (lowercase) |
| `binaryName` | `String` | *required* | Binary executable name |
| `pkgDescription` | `String` | `""` | Homebrew description |
| `homepage` | `String` | `""` | Project homepage URL |
| `javaVersion` | `String` | `"25"` | Required JDK version |
| `tapRepo` | `String` | `""` | GitHub tap repo (e.g. `"ossuminc/homebrew-tap"`) |

**New sbt tasks:**

```scala
val homebrewGenerate = taskKey[File](
  "Generate Homebrew formula .rb file"
)
val homebrewFormulaName = settingKey[String](
  "Homebrew formula name"
)
```

**Behavior of `homebrewGenerate`:**

1. Depends on `Universal/packageBin` to produce the `.zip` artifact
2. Computes SHA256 of the `.zip` file
3. Generates `Formula/<formulaName>.rb` in `target/homebrew/`:

```ruby
class Riddlc < Formula
  desc "Compiler for the RIDDL language"
  homepage "https://ossum.tech/riddl/"
  url "https://github.com/ossuminc/riddl/releases/download/#{version}/riddlc.zip"
  sha256 "COMPUTED_SHA256"
  license "Apache-2.0"

  depends_on "openjdk@25"

  def install
    libexec.install Dir["*"]
    bin.install_symlink Dir["#{libexec}/bin/*"]
  end

  test do
    system "#{bin}/riddlc", "--help"
  end
end
```

4. Returns the `.rb` file path
5. **Note**: Publishing the formula to the tap repo is a separate git
   operation (push to `ossuminc/homebrew-tap`), not handled by this
   task

### With.Packaging.linux(...)

Creates tar.gz archives of native binaries for Linux distribution.

```scala
// In build.sbt
.nativeConfigure(
  With.Packaging.linux(
    pkgName = "riddlc",
    pkgDescription = "RIDDL Language Compiler"
  )
)
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `pkgName` | `String` | *required* | Archive base name |
| `pkgDescription` | `String` | `""` | Package description |
| `includeReadme` | `Boolean` | `true` | Include README in archive |
| `includeLicense` | `Boolean` | `true` | Include LICENSE file |

**New sbt tasks:**

```scala
val linuxPackage = taskKey[File](
  "Create tar.gz archive of native binary for Linux"
)
```

**Behavior:**

1. Depends on `nativeLink` to produce the binary
2. Creates staging directory with:
   - The native binary (from `nativeLink` output)
   - README.md (if `includeReadme`)
   - LICENSE (if `includeLicense`, copied from project root)
3. Creates `<pkgName>-<version>-linux-<arch>.tar.gz`
4. Returns the `.tar.gz` file path

### With.Packaging.windowsMsi(...)

Placeholder API for future Windows MSI support.

```scala
// In build.sbt — NOT YET IMPLEMENTED
.jvmConfigure(
  With.Packaging.windowsMsi(
    pkgName = "riddlc",
    pkgDescription = "RIDDL Language Compiler"
  )
)
```

**Behavior:** Logs a warning that Windows MSI packaging is not yet
implemented. Returns `project` unchanged. This is a placeholder to
reserve the API shape for future work.

---

## File Layout

### New Files

| File | Purpose |
|------|---------|
| `src/main/scala/com/ossuminc/sbt/helpers/NpmPackaging.scala` | npm package assembly + `npm pack` |
| `src/main/scala/com/ossuminc/sbt/helpers/NpmPublishing.scala` | npm registry publishing |
| `src/main/scala/com/ossuminc/sbt/helpers/HomebrewPackaging.scala` | Homebrew formula generation |
| `src/sbt-test/sbt-ossuminc/npm-packaging/` | Scripted test for npm packaging |
| `src/sbt-test/sbt-ossuminc/homebrew/` | Scripted test for Homebrew formula |
| `src/sbt-test/sbt-ossuminc/linux-packaging/` | Scripted test for Linux tar.gz |

### Modified Files

| File | Changes |
|------|---------|
| `helpers/Packaging.scala` | Add `linux()`, `windowsMsi()` methods |
| `helpers/Publishing.scala` | Add `npm()` convenience method |
| `OssumIncPlugin.scala` | No changes needed — helpers already exposed via `With.Packaging` and `With.Publishing` type aliases |

**Note on OssumIncPlugin.scala:** The `With.Packaging` and
`With.Publishing` declarations already point to the `Packaging` and
`Publishing` objects. New methods added to those objects (like
`Packaging.linux()`) are automatically available as
`With.Packaging.linux()`. The new `NpmPackaging` and `NpmPublishing`
objects need new entries:

```scala
// Add to OssumIncPlugin.scala With object:
def NpmPackaging: helpers.NpmPackaging.type = helpers.NpmPackaging
def NpmPublishing: helpers.NpmPublishing.type = helpers.NpmPublishing
def HomebrewPackaging: helpers.HomebrewPackaging.type =
  helpers.HomebrewPackaging
```

Or alternatively, expose them as methods on the existing objects:

```scala
// In Packaging.scala — add delegation methods:
def npm(...)(project: Project): Project =
  NpmPackaging.npm(...)(project)
def homebrew(...)(project: Project): Project =
  HomebrewPackaging.homebrew(...)(project)
def linux(...)(project: Project): Project = // direct implementation
```

The second approach (delegation) is preferred because it keeps the
public API surface at `With.Packaging.npm()` rather than introducing
`With.NpmPackaging`, which would be inconsistent with the existing
`With.Packaging.docker()` pattern.

---

## Implementation Approach

### NpmPackaging.scala

**Key design decisions:**

1. **`npmPrepare` is pure sbt** — No npm binary required. It
   assembles files and generates package.json using sbt's `IO` and
   string templating. This makes CI simpler and testing reliable.

2. **`npmPack` shells out to npm** — Only this task requires npm.
   Uses `sys.process.Process` like the existing `dockerDual` task.

3. **Template mode vs generated mode:**
   - If `npmTemplateFile` is set, read the file and substitute
     `VERSION_PLACEHOLDER` with `version.value`
   - Otherwise, generate package.json from settings using a simple
     JSON string builder (no JSON library dependency needed since
     build.sbt uses Scala 2.12)

4. **TypeScript definitions:** Convention-based discovery. If
   `<module>/js/types/index.d.ts` exists, copy it and add the
   appropriate fields to package.json. Auto-generation from Scala.js
   exports is aspirational and not in scope.

5. **Defaults from Root settings:** Organization, license, and
   repository URL are read from `RootProjectInfo.Keys.*` settings
   that are already populated by `Root(...)`.

**Skeleton:**

```scala
object NpmPackaging {
  object Keys {
    // ... keys defined above
  }

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
      // ... other settings

      Keys.npmPrepare := {
        val log = streams.value.log
        val jsDir = (Compile / fullLinkJS).value
        // ... assembly logic
      },

      Keys.npmPack := {
        val prepDir = Keys.npmPrepare.value
        val targetDir = target.value / "npm-packages"
        IO.createDirectory(targetDir)
        val cmd = Seq("npm", "pack",
          s"--pack-destination=${targetDir.getAbsolutePath}")
        val exitCode = sys.process.Process(cmd, prepDir).!
        // ... error handling
      }
    )
  }
}
```

### NpmPublishing.scala

**Key design decisions:**

1. **Auth via environment variables** — `NPM_TOKEN` for npmjs.com,
   `GITHUB_TOKEN` for GitHub Packages. No sbt credentials file
   (npm uses its own auth mechanism).

2. **Registry-specific tasks** — Both `npmPublishNpmjs` and
   `npmPublishGithub` are available individually, plus `npmPublish`
   runs all configured registries.

3. **Scoped packages** — For GitHub Packages, the package must be
   scoped (`@ossuminc/...`). The scope from `NpmPackaging` settings
   is used to configure the registry URL.

### HomebrewPackaging.scala

**Key design decisions:**

1. **Formula generation only** — Publishing to the tap repo is
   a separate git operation. The task generates the `.rb` file with
   a placeholder URL pattern that includes `#{version}`.

2. **SHA256 from local artifact** — The formula includes the SHA256
   of the locally-built `.zip` file. For release workflows, this
   value should be updated after uploading to GitHub Releases (the
   URL changes).

3. **Template-based** — Uses a Scala string template for the Ruby
   formula. No ERB or Ruby dependencies.

### Packaging.linux() addition

Added directly to existing `Packaging.scala` since it's
straightforward:

```scala
def linux(
  pkgName: String,
  pkgDescription: String = "",
  includeReadme: Boolean = true,
  includeLicense: Boolean = true
)(project: Project): Project = {
  project.settings(
    Keys.linuxPackage := {
      val log = streams.value.log
      val binary = (Compile / nativeLink).value
      val ver = version.value
      val staging = target.value / s"$pkgName-$ver"
      IO.createDirectory(staging)
      IO.copyFile(binary, staging / pkgName)
      // ... readme, license
      val tarball = target.value /
        s"$pkgName-$ver-linux-amd64.tar.gz"
      sys.process.Process(Seq(
        "tar", "czf", tarball.getAbsolutePath,
        "-C", target.value.getAbsolutePath,
        s"$pkgName-$ver"
      )).!
      tarball
    }
  )
}
```

---

## Scripted Test Strategy

### npm-packaging test

```
src/sbt-test/sbt-ossuminc/npm-packaging/
├── build.sbt           # CrossModule with npm config
├── project/
│   ├── build.properties
│   └── plugins.sbt
├── src/main/scala/
│   └── Main.scala      # Minimal @JSExportTopLevel
├── js/types/
│   └── index.d.ts      # Minimal TypeScript defs
├── js/package.json.template  # Template with VERSION_PLACEHOLDER
└── test                # Scripted test script
```

**test script:**
```
> npmPrepare
$ exists target/npm-package/package.json
$ exists target/npm-package/main.js
$ exists target/npm-package/index.d.ts
# Verify version substitution
> checkPackageJsonVersion
```

### homebrew test

```
src/sbt-test/sbt-ossuminc/homebrew/
├── build.sbt           # Program with homebrew config
├── project/
│   ├── build.properties
│   └── plugins.sbt
├── src/main/scala/
│   └── Main.scala      # Minimal main class
└── test
```

**test script:**
```
> homebrewGenerate
$ exists target/homebrew/Formula/test-app.rb
# Verify SHA256 is present
> checkFormulaContent
```

### linux-packaging test

```
src/sbt-test/sbt-ossuminc/linux-packaging/
├── build.sbt           # Native module with linux config
├── project/
│   ├── build.properties
│   └── plugins.sbt
├── src/main/scala/
│   └── Main.scala      # Minimal main
└── test
```

**test script:**
```
> linuxPackage
$ exists target/test-app-*-linux-amd64.tar.gz
```

---

## Implementation Phases

### Phase 1: NpmPackaging (highest value)

**Why first:** Replaces the most hand-rolled code in riddl
(`pack-npm-modules.sh`, CI workflow npm assembly logic).

**Deliverables:**
- `NpmPackaging.scala` with Keys, `npm()` method
- `npmPrepare` and `npmPack` tasks
- Template mode (VERSION_PLACEHOLDER) and generated mode
- TypeScript definitions convention (auto-copy from `js/types/`)
- Scripted test: `npm-packaging`
- Update `Packaging.scala` with `npm()` delegation method

**Testing:** Verify with riddl's `riddlLib` module as integration
test after publishing sbt-ossuminc locally.

### Phase 2: NpmPublishing

**Why second:** Pairs naturally with Phase 1 for end-to-end npm
workflow.

**Deliverables:**
- `NpmPublishing.scala` with `npmPublish*` tasks
- Registry configuration (npmjs, github)
- Auth token from env vars
- Update `Publishing.scala` with `npm()` delegation method
- No scripted test (requires real registry credentials)

### Phase 3: Linux tar.gz packaging

**Why third:** Straightforward implementation, low complexity.

**Deliverables:**
- Add `linux()` method to `Packaging.scala`
- `linuxPackage` task
- Scripted test: `linux-packaging`

### Phase 4: Homebrew formula generation

**Why fourth:** Depends on Phase 3 conceptually (needs a distributable
artifact and its SHA256).

**Deliverables:**
- `HomebrewPackaging.scala` with `homebrew()` method
- `homebrewGenerate` task
- Formula template with URL pattern and SHA256
- Scripted test: `homebrew`
- Update `Packaging.scala` with `homebrew()` delegation method

### Phase 5: Windows MSI placeholder

**Deliverables:**
- Add `windowsMsi()` stub method to `Packaging.scala`
- Logs warning, returns project unchanged
- No scripted test

### Phase 6: Documentation and Release

**Deliverables:**
- Update `README.md` with new helpers documentation
- Update `CLAUDE.md` with new patterns
- Update `NOTEBOOK.md` with completion status
- Tag and publish as sbt-ossuminc 1.3.0

---

## Migration Guide for riddl

After sbt-ossuminc 1.3.0 is published, the riddl project should:

### 1. Replace pack-npm-modules.sh

**Before:**
```bash
./scripts/pack-npm-modules.sh riddlLib
```

**After (build.sbt):**
```scala
lazy val riddlLib_cp = CrossModule("riddlLib", "riddl-lib")(JS, JVM, Native)
  // ... existing config ...
  .jsConfigure(
    With.Packaging.npm(
      scope = "@ossuminc",
      pkgName = "riddl-lib",
      pkgDescription = "RIDDL Language Library - JavaScript/TypeScript bindings",
      keywords = Seq("riddl", "ddd", "domain-driven-design", "parser",
        "ast", "typescript", "reactive-systems"),
      templateFile = Some(file("riddlLib/js/package.json.template"))
    )
  )
```

**sbt command:**
```bash
sbt riddlLibJS/npmPack
# Output: riddlLib/js/target/npm-packages/@ossuminc-riddl-lib-<version>.tgz
```

### 2. Simplify npm-publish.yml

Replace the 100+ lines of npm package assembly in the GitHub Actions
workflow with:

```yaml
- name: Build and pack npm package
  run: sbt riddlLibJS/npmPack

- name: Publish to npmjs.com
  env:
    NPM_TOKEN: ${{ secrets.NPM_TOKEN }}
  run: sbt riddlLibJS/npmPublishNpmjs
```

### 3. Add Homebrew formula generation

```scala
lazy val riddlc_cp = CrossModule("riddlc", "riddlc")(JVM, Native)
  // ... existing config ...
  .jvmConfigure(
    With.Packaging.homebrew(
      formulaName = "riddlc",
      binaryName = "riddlc",
      pkgDescription = "Compiler for the RIDDL language",
      homepage = "https://ossum.tech/riddl/",
      javaVersion = "25",
      tapRepo = "ossuminc/homebrew-tap"
    )
  )
```

### 4. Files to remove from riddl after migration

- `scripts/pack-npm-modules.sh` — replaced by `npmPrepare`/`npmPack`
- Most of `npm-publish.yml` assembly logic — replaced by
  `npmPublish` task
- `riddlLib/js/package.json.template` — can keep for template mode,
  or switch to generated mode

### 5. Files to keep in riddl

- `riddlLib/js/types/index.d.ts` — TypeScript definitions (convention
  location, auto-discovered by `npmPrepare`)
- `Dockerfile` — Multi-stage Docker build (sbt-native-packager's
  Docker support and `With.Packaging.docker()` handle the sbt
  integration; the standalone Dockerfile is for the multi-stage
  build with custom JRE via jlink, which is beyond what
  sbt-native-packager generates)
- `.github/workflows/docker-publish.yml` — Docker CI workflow

---

## Design Decisions

| Decision | Rationale | Alternative |
|----------|-----------|-------------|
| Separate NpmPackaging from NpmPublishing | Different concerns, different auth, easier testing | Single helper |
| `npmPrepare` is pure sbt | No npm dependency for assembly; CI-friendly | Shell out to npm init |
| Template mode with VERSION_PLACEHOLDER | Backward compatible with riddl's existing template | Always generate |
| TypeScript defs by convention (`js/types/`) | Zero config for common case | Explicit setting only |
| Delegation from Packaging/Publishing | Keeps API at `With.Packaging.npm()` | New top-level `With.NpmPackaging` |
| Formula generation only (no tap push) | Tap publishing is a git operation, not packaging | Integrated tap push |
| Homebrew SHA256 from local artifact | Simple, deterministic | Download from GitHub and hash |
| linux() in Packaging.scala directly | Simple enough, no separate file needed | Separate LinuxPackaging.scala |
| windowsMsi() as placeholder | Reserve API shape for future | Skip entirely |
| No JSON library for package.json | Build uses Scala 2.12; string template is sufficient | Add circe/play-json |

---

## Open Questions

1. **Should `npmPrepare` run `fullLinkJS` or `fullOptJS`?**
   Scala.js has deprecated `fullOptJS` in favor of `fullLinkJS`.
   Recommendation: Use `fullLinkJS` (the modern API).

2. **Should Homebrew formula support both JVM and Native variants?**
   Currently the riddl Homebrew formula uses the JVM universal
   package. Native binary would be simpler (no JDK dependency) but
   larger. Recommendation: Support both via a `variant` parameter.

3. **Should linux packaging detect architecture automatically?**
   Native compilation produces a binary for the host architecture.
   The task should detect `amd64` vs `arm64` from the build
   environment. Recommendation: Yes, detect automatically.

---

## Dependencies

- sbt-scalajs (already a dependency) — for `fullLinkJS` task
- sbt-native-packager (already a dependency) — for `Universal/packageBin`
- npm (runtime, PATH) — only for `npmPack` and `npmPublish*` tasks
- tar (runtime, PATH) — only for `linuxPackage` task

No new sbt plugin dependencies are needed.

---

## Compatibility

- **Backward compatible** — All new methods are additions; no
  existing APIs change
- **Scala 2.12** — All helper code must use Scala 2.12 syntax
  (sbt build definitions)
- **sbt 1.x** — Standard sbt APIs (settings, tasks, commands)
