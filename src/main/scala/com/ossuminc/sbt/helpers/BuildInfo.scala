package com.ossuminc.sbt.helpers
import sbt.*
import sbt.Keys.*
import sbtbuildinfo.{BuildInfoKey, BuildInfoPlugin}
import sbtbuildinfo.BuildInfoPlugin.autoImport.*
import sbtbuildinfo.BuildInfoOption.{BuildTime, ToJson, ToMap}

import java.util.Calendar

object BuildInfo extends AutoPluginHelper {

  override def usedHelpers: Seq[AutoPluginHelper] = Seq(ProjectInfo)

  /** The configuration function to call for this plugin helper
    *
    * @param project
    *   The project to which the configuration should be applied
    *
    * @return
    *   The same project passed as an argument, post configuration
    */
  def configure(project: Project): Project = {
    project
      .enablePlugins(BuildInfoPlugin)
      .settings(
        buildInfoObject := "BuildInfo",
        buildInfoPackage := "com.ossuminc",
        buildInfoOptions := Seq(ToMap, ToJson, BuildTime),
        buildInfoUsePackageAsPath := true,
        buildInfoKeys ++= Seq[BuildInfoKey](
          name,
          normalizedName,
          description,
          version,
          organization,
          organizationName,
          BuildInfoKey.map(organizationHomepage) { case (k, v) =>
            k -> v.get.toString
          },
          BuildInfoKey.map(homepage) { case (_, v) =>
            "projectHomepage" -> v.map(_.toString).getOrElse(ProjectInfo.Keys.projectHomePage.value)
          },
          BuildInfoKey.map(licenses) { case (k, v) =>
            k -> v.map(_._1).mkString(", ")
          },
          isSnapshot,
          buildInfoPackage,
          buildInfoObject,
          BuildInfoKey.map(startYear) { case (k, v) =>
            k -> v.map(_.toString).getOrElse(ProjectInfo.Keys.projectStartYear.toString)
          },
          BuildInfoKey.map(startYear) { case (_, v) =>
            "copyright" -> s"© ${v.map(_.toString).getOrElse(ProjectInfo.Keys.projectStartYear.value.toString)}-${Calendar
                .getInstance()
                .get(Calendar.YEAR)} ${organizationName.value}"
          },
          scalaVersion,
          sbtVersion,
          BuildInfoKey.map(scalaVersion) { case (_, v) =>
            val version = if (v.head == '2') {
              v.substring(0, v.lastIndexOf('.'))
            } else v
            "scalaCompatVersion" -> version
          }
        )
      )

  }
}
