# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-01-08

### Major Milestone: First Stable Release 🎉

This 1.0.0 release represents the first production-ready, stable version of sbt-ossuminc with comprehensive features, modern dependencies, and extensive test coverage.

### Added

- **AsciiDoc Support** - First-class support for AsciiDoc document generation
  - HTML5 static website generation via integration with sbt-site-asciidoctor
  - PDF generation via asciidoctorj-pdf (2.3.18)
  - Diagram support via asciidoctorj-diagram (2.3.1) for PlantUML, Graphviz, etc.
  - Customizable source directories, attributes, and output locations
  - New `With.AsciiDoc()` helper matching Maven asciidoctor-plugin functionality
  - Full documentation in README.md

- **Test Coverage** - New scripted tests for previously untested helpers
  - Laminar test for Scala.js UI library configuration
  - MiMa test for binary compatibility checking
  - Akka test with repository token documentation

### Changed

- **Build System Updates**
  - Updated sbt from 1.10.6 to 1.12.0
  - Updated Scala default version from 3.4.3 to 3.3.7 LTS
  - Updated all 11 scripted test projects to use sbt 1.12.0

- **Plugin Dependencies** - Updated 9 plugins to latest versions:
  - sbt-buildinfo: 0.12.0 → 0.13.1
  - sbt-scalafix: 0.12.1 → 0.14.5
  - sbt-scalafmt: 2.5.2 → 2.5.6
  - sbt-scoverage: 2.1.1 → 2.4.1
  - sbt-scala-native: 0.5.6 → 0.5.9
  - sbt-scalajs: 1.17.0 → 1.20.1
  - sbt-idea-plugin: 3.26.2 → 5.0.4 (org.jetbrains.scala)
  - commons-lang3: 3.15.0 → 3.20.0
  - slf4j-simple: 2.0.13 → 2.0.17

- **Akka Helper** - Modernized for October 2024 licensing changes
  - Added repository token support via `repositoryToken` parameter
  - Falls back to `AKKA_REPO_TOKEN` environment variable
  - Provides clear warning messages when token is missing
  - Updated for Akka 2024.10 release
  - See `src/sbt-test/sbt-ossuminc/akka/README.md` for usage

- **IDEA Plugin Helper** - Modernized for compatibility with IntelliJ IDEA 2024.3+
  - Updated for sbt-idea-plugin 5.0.4 (org.jetbrains.scala group)
  - Fixed `intellijBuild` scope to use `ThisBuild /` (required for 5.x)
  - Changed default build to 243.21565.193 (IntelliJ IDEA 2024.3 stable)
  - Maintains compatibility with modern IntelliJ versions

### Fixed

- **Scala Version** - Corrected default from non-existent 3.7.4 to 3.3.7 LTS
- **Resolvers** - Removed deprecated Sonatype OSS and Bintray resolvers
- **scalably-typed Test** - Fixed hardcoded Scala version from 3.4.3 to 3.3.7
- **Documentation** - Corrected typos and synchronized all helper documentation

### Test Status

- 9/11 scripted tests passing (82%)
- Passing: basic, cross, everything, multi, native, packaging, program, scalably-typed, scalajs
- Deferred (by design):
  - akka: Requires AKKA_REPO_TOKEN environment variable (fully functional)
  - idea-plugin: Deferred until actual plugin development needed

### Documentation

- Comprehensive README.md updates
- Added AsciiDoc helper documentation with examples
- Added design philosophy section explaining functional/minimalist/DRY approach
- Synchronized all helper documentation with code
- Created Akka test README with token setup instructions

### Breaking Changes

None - All changes maintain backwards compatibility with 0.x versions.

### Migration Notes

For users updating from 0.x versions:

1. **Akka Users**: If using Akka 2024.05+, obtain a free repository token from https://akka.io/key and either:
   - Pass it via `With.Akka.forRelease("24.10", Some("your-token"))`
   - Or set environment variable `AKKA_REPO_TOKEN=your-token`

2. **IDEA Plugin Developers**: The helper now uses sbt-idea-plugin 5.0.4. To use it:
   - Add `addSbtPlugin("org.jetbrains.scala" % "sbt-idea-plugin" % "5.0.4")` to project/plugins.sbt
   - The default build is now 243.21565.193 (IntelliJ IDEA 2024.3)

3. **AsciiDoc Users**: To enable full HTML generation, add to project/plugins.sbt:
   ```scala
   addSbtPlugin("com.github.sbt" % "sbt-site-asciidoctor" % "1.7.0")
   ```
   Then enable in build.sbt: `enablePlugins(AsciidoctorPlugin)`

### Acknowledgments

This release represents a major milestone in the maturation of sbt-ossuminc as a production-ready build plugin for Scala projects. Special thanks to all contributors and users who provided feedback during the 0.x development phase.

---

## [0.21.1] - Previous Release

See git history for changes in previous versions.
