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

import sbt.Project

/** A Little Help For AutoPlugins This trait just provides some definitions and makes it easier to
  * set up the plugin requirements. Just list the helpers upon which your plugin is dependent in the
  * autoPlugins method and the rest is taken care of.
  */
trait AutoPluginHelper extends {

  /** The typical configuration function type for this helper
    * @param project
    *   The project to which the configuration should be applied
    * @return
    *   The same project passed as an argument, but modified with the configuration this helper
    *   provides.
    */

  def configure(project: Project): Project
}
