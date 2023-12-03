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

import RootProjectInfo.Keys.projectStartYear
import sbt.*
import sbt.Keys.*
import de.heikoseeberger.sbtheader
import de.heikoseeberger.sbtheader.HeaderPlugin

import java.time.Year

object Header extends AutoPluginHelper {

  def configure(project: Project): Project = {
    val copyright_holder = "Ossum Inc."
    import HeaderPlugin.autoImport._
    import sbtheader.{CommentStyle, FileType}
    project
      .enablePlugins(HeaderPlugin)
      .settings(
        headerEmptyLine := true,
        startYear := Some(projectStartYear.value),
        headerLicense := {
          val years = startYear.value.get.toString + "-" + Year.now().toString
          Some(
            HeaderLicense.ALv2(
              years,
              copyright_holder,
              HeaderLicenseStyle.SpdxSyntax
            )
          )
        },
        headerMappings ++= Map[FileType, CommentStyle](
          FileType.sh -> CommentStyle.hashLineComment,
          FileType(".sbt") -> CommentStyle.cStyleBlockComment,
          FileType(".xml") -> CommentStyle.xmlStyleBlockComment,
          FileType(".scala.html") -> CommentStyle.twirlStyleBlockComment,
          FileType(".conf") -> CommentStyle.hashLineComment
        )
      )
  }
}
