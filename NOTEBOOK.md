# Engineering Notebook: sbt-ossuminc

## Current Status

Version 1.1.0 has been tagged and pushed but not yet published. The build is currently configured for Sonatype publishing, but we need to switch to GitHub Packages.

## Work Completed (Recent)

- [x] Fixed ScalaJS helper to handle missing git commit/scmInfo gracefully
- [x] Added deprecated `With.Javascript` alias for backward compatibility
- [x] Updated all scripted tests to pass (14/14)
- [x] Updated README.md with correct API documentation
- [x] Committed and tagged v1.1.0
- [x] Pushed to origin/main

## In Progress

- [ ] Switch publishing from Sonatype to GitHub Packages

## Plan: Switch to GitHub Packages Publishing

### Background

The `project/` directory uses symlinks to reference helper files from `src/main/scala/com/ossuminc/sbt/helpers/`. This allows the plugin to use its own functionality during its own build (bootstrapping).

Current symlinks in `project/`:
- `AutoPluginHelper.scala` → `../src/main/scala/com/ossuminc/sbt/helpers/AutoPluginHelper.scala`
- `DynamicVersioning.scala` → `../src/main/scala/com/ossuminc/sbt/helpers/DynamicVersioning.scala`
- `Miscellaneous.scala` → `../src/main/scala/com/ossuminc/sbt/helpers/Miscellaneous.scala`
- `Release.scala` → `../src/main/scala/com/ossuminc/sbt/helpers/Release.scala`
- `RootProjectInfo.scala` → `../src/main/scala/com/ossuminc/sbt/helpers/RootProjectInfo.scala`
- `Scala2.scala` → `../src/main/scala/com/ossuminc/sbt/helpers/Scala2.scala`
- `SonatypePublishing.scala` (local file, not symlink)

### Steps to Switch to GitHub Packages

#### Step 1: Create symlink for GithubPublishing

```bash
cd /Users/reid/Code/ossuminc/sbt-ossuminc/project
ln -s ../src/main/scala/com/ossuminc/sbt/helpers/GithubPublishing.scala GithubPublishing.scala
```

#### Step 2: Update build.sbt

Change the import from:
```scala
import com.ossuminc.sbt.helpers.{DynamicVersioning, RootProjectInfo, Scala2, SonatypePublishing}
```

To:
```scala
import com.ossuminc.sbt.helpers.{DynamicVersioning, RootProjectInfo, Scala2, GithubPublishing}
```

Change the configure call from:
```scala
.configure(SonatypePublishing.configure)
```

To:
```scala
.configure(GithubPublishing.configure)
```

#### Step 3: Set up GitHub Token

The `GithubPublishing` helper uses `sbt-github-packages` which looks for tokens in this order:
1. Git config: `git config github.token`
2. Environment variable: `GITHUB_TOKEN`

**Token Requirements:**
- Must have `write:packages` scope for publishing
- Must have `read:packages` scope for reading (if using private packages)
- For organization repos, the token owner must have appropriate org permissions

**To set up the token:**

Option A - Environment variable (recommended for CI and one-time use):
```bash
export GITHUB_TOKEN=ghp_your_token_here
```

Option B - Git config (persistent local setup):
```bash
git config --global github.token ghp_your_token_here
```

#### Step 4: Publish

After setting `GITHUB_TOKEN`:
```bash
sbt publish
```

### How GithubPublishing Works

The `GithubPublishing` helper (`src/main/scala/com/ossuminc/sbt/helpers/GithubPublishing.scala`):

```scala
object GithubPublishing extends AutoPluginHelper {
  def configure(project: Project): Project = {
    project
      .enablePlugins(sbtghpackages.GitHubPackagesPlugin)
      .settings(
        githubOwner := RootProjectInfo.Keys.gitHubOrganization.value,      // "ossuminc"
        githubRepository := RootProjectInfo.Keys.gitHubRepository.value,   // "sbt-ossuminc"
        githubTokenSource := TokenSource.Or(
          TokenSource.GitConfig("github.token"),
          TokenSource.Environment("GITHUB_TOKEN")
        ),
        publishMavenStyle := true,
        resolvers += Resolver.githubPackages(RootProjectInfo.Keys.gitHubOrganization.value),
        publishTo := githubPublishTo.value
      )
  }
}
```

The `RootProjectInfo.initialize("sbt-ossuminc", startYr = 2015)` call in `build.sbt` sets:
- `gitHubOrganization` = "ossuminc" (default)
- `gitHubRepository` = "sbt-ossuminc"

### Verification Checklist

Before publishing:
- [ ] `GITHUB_TOKEN` environment variable is set
- [ ] Token has `write:packages` scope
- [ ] Symlink created: `project/GithubPublishing.scala`
- [ ] `build.sbt` updated to use `GithubPublishing`
- [ ] Run `sbt compile` to verify build still works
- [ ] Run `sbt publish` to publish

### After Publishing

Update users to reference the new location in their `~/.sbt/1.0/github.sbt`:
```scala
credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "your-github-username",
  "your-github-token-with-read:packages-scope"
)
```

## Design Decisions Log

| Decision | Rationale | Alternatives Considered | Date |
|----------|-----------|------------------------|------|
| Switch from Sonatype to GitHub Packages | Sonatype publishing is deprecated for this project; GitHub Packages provides simpler auth for ossuminc org | Continue with Sonatype, use both | 2026-01-15 |
| Use symlink approach | Consistent with existing pattern in project/ directory | Copy file, inline the settings | 2026-01-15 |
| Renamed With.Javascript to With.ScalaJS | Clearer naming, matches the actual technology | Keep old name, add both | 2026-01-15 |

## Next Steps

1. **In new session with GITHUB_TOKEN set:**
   - Create the GithubPublishing symlink
   - Update build.sbt
   - Test with `sbt compile`
   - Publish with `sbt publish`

2. Commit the build.sbt changes (minor change, could be v1.1.1 or just a fix commit)

## Open Questions

- Should we remove the `project/SonatypePublishing.scala` file after switching?
- Do we need to update CI/CD workflows for GitHub Actions?
