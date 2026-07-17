# Ship Skill

Executes a full release cycle for the sbt-ossuminc plugin.
Follow each step in order. **STOP immediately** if any
assertion fails and report the problem.

## Arguments

The user should provide a version number (e.g., `1.3.6`). If
not provided:
1. Run `git tag --sort=-v:refname | head -5` to find the
   actual latest tag (not just reachable from current branch)
2. Run `git log --oneline <latest-tag>..HEAD` to see changes
3. Analyze per semver and **recommend** a version (don't ask
   the user to choose — present your recommendation and let
   them confirm or override)

## Pre-Flight Checks

1. Assert current branch is `main`:
   ```
   git branch --show-current
   ```
   If not on `main`, ask the user before switching. **Never
   publish from `development` or feature branches.**

2. Assert working tree is clean:
   ```
   git status --porcelain
   ```
   If dirty, list the uncommitted files and ask the user how
   to proceed.

3. **GITHUB_TOKEN handling**: Do NOT `unset GITHUB_TOKEN`
   globally — sbt needs it for GitHub Packages resolution.
   Only unset it immediately before `gh` commands:
   ```
   unset GITHUB_TOKEN && gh ...
   ```

4. When switching to `main`, always `git pull` first to
   ensure local main is up to date with origin.

5. Verify the version tag does not already exist:
   ```
   git tag -l <VERSION>
   ```

## Ship Steps

6. **Ensure the working tree is clean before tagging.**
   sbt-dynver derives the version from `git describe`;
   any dirty tree causes it to append a
   `<VERSION>-N-<hash>-<timestamp>` suffix instead of
   the clean `<VERSION>`. This suffix would propagate
   to all published artifacts. Check:
   ```
   git status --porcelain
   ```
   If any files are modified or untracked, commit them
   now before proceeding.

7. Create an annotated git tag:
   ```
   git tag -a <VERSION> -m "Release <VERSION>"
   ```

8. Verify dynver resolves to the clean version:
   ```
   sbt 'show version'
   ```
   The output must be exactly `<VERSION>` with no suffix.
   If it has a suffix, do NOT proceed — delete the tag
   (`git tag -d <VERSION>`), fix the issue, and re-tag.

9. Run the full local test gate — **do NOT publish here**:
   ```
   sbt "; clean; test; scripted"
   ```
   (sbt 2 parses CLI args as one command line — commands
   MUST be `;`-separated; `sbt clean test scripted` is
   parsed as a single unknown command and fails.)

   **Why no `publish` in this step:** the `release.yml`
   workflow triggered in step 11 is the *sole* publisher
   (it publishes to GitHub Packages and uploads the JAR to
   the release). Publishing locally here first makes the
   workflow's publish fail with a GitHub Packages
   `PUT ... overwriting is disabled` 409 — the version
   already exists — and the workflow then dies before
   uploading the JAR asset. Let the workflow own publishing.

   Scripted tests validate the plugin from a consumer's
   perspective. Do NOT skip them.
   **If tests fail, delete the tag** (`git tag -d
   <VERSION>`) — do NOT push a broken release.

   **dynver / cache gotcha:** sbt-dynver computes the
   version once at sbt startup from `git describe`, and
   sbt 2's disk cache (`~/Library/Caches/sbt`, `~/.cache/sbt`)
   can serve a *stale* version from before the tag/fetch. If
   `show version` (step 8) showed a suffix or wrong base,
   purge those dirs plus `target` and re-run in a fresh sbt
   session before proceeding.

10. Push commits and tag to origin:
    ```
    git push origin main <VERSION>
    ```

11. Create a GitHub release:
    ```
    unset GITHUB_TOKEN && gh release create <VERSION> \
      --title "Release <VERSION>" --generate-notes
    ```
    This triggers the `release.yml` workflow which also
    uploads JAR artifacts to the release.

## Post-Release Verification

12. Confirm the release exists:
    ```
    unset GITHUB_TOKEN && gh release view <VERSION>
    ```

13. Run `git status` to confirm the working tree is clean.

14. Switch back to `development` and merge the tag forward:
    ```
    git checkout development
    git merge main
    git push
    ```

15. Report a summary: tag, commit SHA, release URL, and any
    CI workflows triggered.

16. **Drop upgrade tasks in dependent projects.** For each
    consumer of sbt-ossuminc, create a task file in its
    `task/` directory describing the plugin version bump
    needed. The file should be named
    `upgrade-sbt-ossuminc-<VERSION>.md` and contain:
    - What changed (link to the GitHub release)
    - The new version to depend on
    - The file to update (`project/plugins.sbt`)

    Consumer projects:
    ```
    ../riddl/task/upgrade-sbt-ossuminc-<VERSION>.md
    ../synapify/task/upgrade-sbt-ossuminc-<VERSION>.md
    ../riddl-idea-plugin/task/upgrade-sbt-ossuminc-<VERSION>.md
    ../riddlsim/task/upgrade-sbt-ossuminc-<VERSION>.md
    ../ossum.tech/task/upgrade-sbt-ossuminc-<VERSION>.md
    ../riddl-mcp-server/task/upgrade-sbt-ossuminc-<VERSION>.md
    ../riddl-models/task/upgrade-sbt-ossuminc-<VERSION>.md
    ../ossum-ai-api/task/upgrade-sbt-ossuminc-<VERSION>.md
    ../riddl-gen/task/upgrade-sbt-ossuminc-<VERSION>.md
    ../riddl-examples/task/upgrade-sbt-ossuminc-<VERSION>.md
    ../riddl-server-infrastructure/task/upgrade-sbt-ossuminc-<VERSION>.md
    ```

## If Something Fails

- If tests fail in step 9: delete the local tag
  (`git tag -d <VERSION>`), fix, and restart from step 6.
  Do NOT push a broken tag.
- If scripted tests fail in step 9: this means the plugin
  is broken for consumers. Fix before releasing.
- If dynver shows a suffix in step 8: it is usually a stale
  sbt disk cache, not a dirty tree — purge
  `~/Library/Caches/sbt`, `~/.cache/sbt`, and `target`, then
  re-run `show version` in a fresh sbt session. If the tree
  is genuinely dirty, delete the tag, commit/clean, re-tag.
- If tag push fails in step 10: check if tag exists
  remotely.
- If the `release.yml` publish fails in step 11 with a
  GitHub Packages "overwriting is disabled" 409: the version
  was already published (e.g. a local `publish` was run by
  mistake). The artifacts are already on GitHub Packages, so
  the release is functional; just attach the JAR the workflow
  never uploaded:
  `gh release upload <VERSION> target/out/jvm/scala-*/sbt-ossuminc/sbt-ossuminc_sbt2_3-<VERSION>.jar --clobber`.
- If `gh release create` fails in step 11: the tag is
  already pushed, so the release can be created manually
  or retried.
- **Never force-push tags** without explicit user approval.
