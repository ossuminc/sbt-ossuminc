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

import RootProjectInfo.Keys.{copyrightHolder, projectStartYear}
import sbt.*
import sbt.Keys.*
import de.heikoseeberger.sbtheader
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.{headerEndYear, headerLicense, headerLicenseStyle}
import de.heikoseeberger.sbtheader.License.*
import de.heikoseeberger.sbtheader.LicenseStyle.SpdxSyntax
import de.heikoseeberger.sbtheader.{AutomateHeaderPlugin, HeaderPlugin}

import java.time.Year
import java.util.Calendar

object Header extends AutoPluginHelper {

  def configure(project: Project): Project = {
    import HeaderPlugin.autoImport._
    import sbtheader.{CommentStyle, FileType}
    project
      .enablePlugins(AutomateHeaderPlugin)
      .settings(
        headerEmptyLine := true,
        startYear := Some(projectStartYear.value),
        headerLicenseStyle := SpdxSyntax,
        headerEndYear := Some(Calendar.getInstance().get(Calendar.YEAR)),
        headerLicense := {
          val years = startYear.value.get.toString + "-" + Year.now().toString
          Some(
            HeaderLicense.ALv2(
              years,
              copyrightHolder.value,
              HeaderLicenseStyle.Detailed
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
  def specificLicense(spdx: String)(project: Project): Project = {
    configure(project)
      .settings(
        headerLicense := {
          val startYr = projectStartYear.value.toString
          val orgName = organizationName.value
          val lic = spdx match {
            case l @ "Apache-2.0"        => ALv2(startYr, orgName, SpdxSyntax)
            case l @ "MIT"               => MIT(startYr, orgName, SpdxSyntax)
            case l @ "MPLv2"             => MPLv2(startYr, orgName, SpdxSyntax)
            case l @ "BSD-2-Clause"      => BSD2Clause(startYr, orgName, SpdxSyntax)
            case l @ "BSD-3-Clause"      => BSD3Clause(startYr, orgName, SpdxSyntax)
            case l @ "GPL-3.0"           => GPLv3(startYr, orgName, SpdxSyntax)
            case l @ "GPL-3.0-or-later"  => GPLv3OrLater(startYr, orgName, SpdxSyntax)
            case l @ "GPL-3.0-only"      => GPLv3Only(startYr, orgName, SpdxSyntax)
            case l @ "LGPL-3.0-only"     => LGPLv3Only(startYr, orgName, SpdxSyntax)
            case l @ "LGPL-3.0-or-later" => LGPLv3OrLater(startYr, orgName, SpdxSyntax)
            case l @ "LGPL-3.0"          => LGPLv3(startYr, orgName, SpdxSyntax)
            case l @ "AGPL-3.0"          => AGPLv3(startYr, orgName, SpdxSyntax)
          }
          Some(lic)
        }
      )
  }
}
