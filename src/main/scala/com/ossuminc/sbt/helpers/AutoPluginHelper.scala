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

/** A Little Help For AutoPlugins. This trait extends `(Project => Project)`
  * so helpers can be used directly as configuration functions:
  * `.configure(With.Scala3)` instead of `.configure(With.Scala3.configure)`.
  */
trait AutoPluginHelper extends (Project => Project) {

  /** Apply this helper's configuration to a project.
    * @param project
    *   The project to which the configuration should be applied
    * @return
    *   The same project with this helper's configuration applied.
    */
  def apply(project: Project): Project
}
