/*
 * Copyright 2015-2026 Ossum Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*
import com.ossuminc.sbt.helpers.RootProjectInfo.Keys.{gitHubOrganization, gitHubRepository}

/** Configure publishing to Maven Central via the Sonatype **Central Portal**.
  *
  * sbt 2.x ships native Central Portal support, so this helper does not use the
  * (deprecated) `sbt-sonatype` plugin. The legacy OSSRH host was sunset; Central
  * Portal is the current path:
  *
  *   - **snapshots** (`-SNAPSHOT` versions) publish to the Central snapshots repo;
  *   - **releases** publish to the built-in `localStaging` resolver, which stages
  *     a deployment bundle that `sonaUpload` then sends to the Central Portal.
  *
  * Release flow once configured (and after PGP signing is set up):
  * {{{
  *   sbt publishSigned sonaUpload sonaRelease
  * }}}
  * `publishSigned` comes from sbt-pgp (re-exported by this plugin). `sonaUpload`
  * and `sonaRelease` are sbt 2 built-in commands.
  *
  * Credentials are read by sbt 2 natively from either the `SONATYPE_USERNAME` /
  * `SONATYPE_PASSWORD` environment variables (CI) or
  * `~/.sbt/sonatype_central_credentials` (local), so this helper does not set
  * `credentials` itself. PGP signing keys come from `PGP_SECRET` /
  * `PGP_PASSPHRASE` (CI) or the local GPG keyring.
  *
  * @note Do not combine with [[GithubPublishing]].
  */
object SonatypePublishing extends AutoPluginHelper {

  /** The Central Portal snapshots repository. */
  private val centralSnapshots =
    "https://central.sonatype.com/repository/maven-snapshots/"

  def apply(project: Project): Project = {
    project.settings(
      publishMavenStyle := true,
      pomIncludeRepository := { _ => false },
      Test / publishArtifact := false,
      publishTo := {
        if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
        else localStaging.value
      },
      scmInfo := {
        val org = RootProjectInfo.requireConfigured(
          gitHubOrganization.value, "gitHubOrganization", "SonatypePublishing"
        )
        val repo = RootProjectInfo.requireConfigured(
          gitHubRepository.value, "gitHubRepository", "SonatypePublishing"
        )
        Some(
          ScmInfo(
            uri(s"https://github.com/$org/$repo"),
            s"scm:git:https://github.com/$org/$repo.git"
          )
        )
      }
    )
  }
}
