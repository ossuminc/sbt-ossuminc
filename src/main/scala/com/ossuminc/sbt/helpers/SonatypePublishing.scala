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

/** Publishing to Maven Central via Sonatype.
  *
  * sbt 2.x note: the `sbt-sonatype` plugin was deprecated by its author (OSSRH
  * was sunset) and has no sbt 2.0 GA build. Maven Central now uses the Central
  * Portal, whose upload workflow is not yet wired into sbt-ossuminc for sbt 2.
  * Until that follow-up lands, use `With.GithubPublishing`. See NOTEBOOK.md
  * "sbt 2.0 Migration Plan".
  */
object SonatypePublishing extends AutoPluginHelper {

  object Keys {
    val sonatypeServer: SettingKey[String] = settingKey[String](
      "Deprecated on sbt 2.x; Maven Central now uses the Central Portal."
    )
  }

  /** Not yet available on sbt 2.x — fails fast with an explanatory message. */
  def apply(project: Project): Project = sys.error(
    "With.SonatypePublishing is not yet available on sbt 2.x: sbt-sonatype is " +
      "deprecated (OSSRH sunset) and Central Portal publishing is not yet wired " +
      "into sbt-ossuminc for sbt 2. Use With.GithubPublishing, or publish to " +
      "Maven Central manually, until this is implemented."
  )
}
