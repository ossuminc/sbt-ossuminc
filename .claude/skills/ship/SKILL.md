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

9. Run the full test suite including scripted plugin tests
   and publish:
   ```
   sbt clean test scripted publish
   ```
   Scripted tests validate the plugin from a consumer's
   perspective. Do NOT skip them. Because the tag is on
   HEAD and the tree is clean, all published artifacts
   will carry the clean `<VERSION>`. Verify in the sbt
   output.
   **If tests fail, delete the tag** (`git tag -d
   <VERSION>`) — do NOT push a broken release.

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
- If dynver shows a suffix in step 8: delete the tag, fix
  the dirty tree, and re-tag.
- If tag push fails in step 10: check if tag exists
  remotely.
- If publish fails in step 9: check credentials and retry
  (tag is still local, safe to retry).
- If `gh release create` fails in step 11: the tag is
  already pushed, so the release can be created manually
  or retried.
- **Never force-push tags** without explicit user approval.
