/*
 * Copyright 2015-2017 Ossum Inc.
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

/** Release automation support.
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-release.
  *             Use manual release processes or alternative release plugins until
  *             the plugin is updated.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-release plugin", "2.0.0")
object Release extends AutoPluginHelper {

  /** The Keys and types pertaining to releasing artifacts */
  object Keys {
    sealed trait ArtifactKind

    case object ZipFileArtifact extends ArtifactKind
    case object DebianServerArtifact extends ArtifactKind
    case object RPMScalaServerArtifact extends ArtifactKind
    case object DockerServerArtifact extends ArtifactKind
    case object WindowsArtifact extends ArtifactKind

    val artifactKinds: SettingKey[Seq[ArtifactKind]] =
      settingKey[Seq[ArtifactKind]](
        "The kinds of artifacts the project should produce. " +
          "Defaults to just to ZipFileArtifact"
      )
  }

  def apply(project: Project): Project = {
    throw new UnsupportedOperationException(
      "Release is not available in sbt 2.x. " +
        "The sbt-release plugin does not yet support sbt 2.0. " +
        "Please use manual release processes or wait for plugin updates."
    )
  }
}
