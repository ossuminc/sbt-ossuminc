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

/** License header automation support.
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-header plugin.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-header plugin", "2.0.0")
object Header extends AutoPluginHelper {

  def apply(project: Project): Project = {
    throw new UnsupportedOperationException(
      "Header is not available in sbt 2.x. " +
        "The sbt-header plugin does not yet support sbt 2.0. " +
        "Please wait for plugin updates."
    )
  }
}
