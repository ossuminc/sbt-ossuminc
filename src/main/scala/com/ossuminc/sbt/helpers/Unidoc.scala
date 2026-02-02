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

/** Unified Scaladoc documentation support.
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-unidoc plugin.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-unidoc plugin", "2.0.0")
object Unidoc extends AutoPluginHelper {

  object Keys {
    val titleForDocs: SettingKey[String] = settingKey[String](
      "The name of the project as it should appear in documentation."
    )
  }

  def apply(project: Project): Project = {
    project.configure(this.configure())
  }

  def configure(
    apiOutput: File = file("target/unidoc"),
    baseURL: Option[String] = None,
    inclusions: Seq[ProjectReference] = Seq.empty,
    exclusions: Seq[ProjectReference] = Seq.empty,
    logoURL: Option[String] = None,
    externalMappings: Seq[Seq[String]] = Seq.empty
  )(project: Project): Project = {
    throw new UnsupportedOperationException(
      "Unidoc is not available in sbt 2.x. " +
        "The sbt-unidoc plugin does not yet support sbt 2.0. " +
        "Please wait for plugin updates."
    )
  }
}
