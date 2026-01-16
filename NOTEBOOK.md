# Engineering Notebook: sbt-ossuminc

## Current Status

Version 1.1.0 has been released and published locally. The build is now configured for GitHub Packages publishing, but there's an unresolved issue with sbt plugins not resolving correctly from GitHub Packages.

## Work Completed (Recent)

- [x] Fixed ScalaJS helper to handle missing git commit/scmInfo gracefully
- [x] Added deprecated `With.Javascript` alias for backward compatibility
- [x] Updated all scripted tests to pass (14/14)
- [x] Updated README.md with correct API documentation
- [x] Switched from Sonatype to GitHub Packages publishing
- [x] Committed and tagged 1.1.0 (note: no `v` prefix - it interferes with sbt-dynver)
- [x] Pushed to origin/main
- [x] Published 1.1.0 to GitHub Packages (artifacts uploaded)
- [x] Published 1.1.0 locally via `publishLocal`

## Resolved Issues

### GitHub Packages resolution for sbt plugins (FIXED)

**Problem:** Other projects couldn't resolve sbt-ossuminc from GitHub Packages.

**Root cause:** The README was missing the resolver configuration. GitHub Packages is not in sbt's default resolver chain, so consumers must explicitly add it.

**Solution:** Updated README to include the resolver in `project/plugins.sbt`:
```scala
resolvers += "GitHub Packages" at "https://maven.pkg.github.com/ossuminc/sbt-ossuminc"
addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "1.1.0")
```

Plus credentials in `~/.sbt/1.0/github.sbt`.

## Design Decisions Log

| Decision | Rationale | Alternatives Considered | Date |
|----------|-----------|------------------------|------|
| Switch from Sonatype to GitHub Packages | Sonatype publishing is deprecated for this project; GitHub Packages provides simpler auth for ossuminc org | Continue with Sonatype, use both | 2026-01-15 |
| Use symlink approach | Consistent with existing pattern in project/ directory | Copy file, inline the settings | 2026-01-15 |
| Renamed With.Javascript to With.ScalaJS | Clearer naming, matches the actual technology | Keep old name, add both | 2026-01-15 |
| Tag format without `v` prefix | sbt-dynver doesn't recognize tags with `v` prefix; existing tags use `1.0.0` format | Use `v1.1.0` (failed) | 2026-01-15 |

## Project Structure Notes

The `project/` directory uses symlinks to reference helper files from `src/main/scala/com/ossuminc/sbt/helpers/`. This allows the plugin to use its own functionality during its own build (bootstrapping).

Current symlinks in `project/`:
- `AutoPluginHelper.scala` → helpers source
- `DynamicVersioning.scala` → helpers source
- `GithubPublishing.scala` → helpers source (added for 1.1.0)
- `Miscellaneous.scala` → helpers source
- `Release.scala` → helpers source
- `RootProjectInfo.scala` → helpers source
- `Scala2.scala` → helpers source
- `SonatypePublishing.scala` (local file, not symlink - could be removed)

## Next Steps

1. Investigate GitHub Packages sbt plugin resolution issue
2. Consider removing `project/SonatypePublishing.scala` (no longer used)
3. Update CI/CD workflows if needed for GitHub Actions

## Open Questions

- Should we fall back to Sonatype/Maven Central for sbt plugin publishing?
- Is the README missing resolver configuration for consuming the plugin from GitHub Packages?
