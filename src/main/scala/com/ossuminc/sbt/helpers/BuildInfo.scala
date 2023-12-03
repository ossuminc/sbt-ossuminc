package com.ossuminc.sbt.helpers
import sbt._
import sbt.Keys._
import sbtbuildinfo.BuildInfoKeys.buildInfoUsePackageAsPath
import sbtbuildinfo.{BuildInfoKey, BuildInfoPlugin}
import sbtbuildinfo.BuildInfoPlugin.autoImport.*
import sbtbuildinfo.BuildInfoOption.{BuildTime, ToJson, ToMap}

import java.util.Calendar

object BuildInfo extends AutoPluginHelper {

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
          normalizedName,
          description,
          organization,
          organizationName,
          BuildInfoKey.map(organizationHomepage) { case (k, v) =>
            k -> v.get.toString
          },
          BuildInfoKey.map(homepage) { case (_, v) =>
            "projectHomepage" -> v.map(_.toString).getOrElse(RootProjectInfo.Keys.projectHomePage.value)
          },
          BuildInfoKey.map(licenses) { case (k, v) =>
            k -> v.map(_._1).mkString(", ")
          },
          isSnapshot,
          buildInfoPackage,
          buildInfoObject,
          BuildInfoKey.map(startYear) { case (k, v) =>
            k -> v.map(_.toString).getOrElse(RootProjectInfo.Keys.projectStartYear.toString)
          },
          BuildInfoKey.map(startYear) { case (_, v) =>
            "copyright" -> s"© ${v.map(_.toString).getOrElse(RootProjectInfo.Keys.projectStartYear.value.toString)}-${Calendar
                .getInstance()
                .get(Calendar.YEAR)} ${organizationName.value}"
          },
          BuildInfoKey.map(scalaVersion) { case (_, v) =>
            val version = if (v.head == '2') {
              v.substring(0, v.lastIndexOf('.'))
            } else v
            "scalaCompatVersion" -> version
          }
        )
      )

  }

  def withBuildInfo(
    homePage: String,
    orgName: String,
    packageName: String,
    objName: String = "BuildInfo",
    baseYear: Int = 2023
  )(p: Project): Project = {
    p.settings(
      buildInfoObject := objName,
      buildInfoPackage := packageName,
      buildInfoOptions := Seq(ToMap, ToJson, BuildTime),
      buildInfoUsePackageAsPath := true,
      buildInfoKeys ++= Seq[BuildInfoKey](
        description,
        organization,
        organizationName,
        BuildInfoKey.map(organizationHomepage) { case (k, v) =>
          k -> v.get.toString
        },
        BuildInfoKey.map(homepage) { case (k, v) =>
          "projectHomepage" -> v.map(_.toString).getOrElse(homePage)
        },
        BuildInfoKey.map(startYear) { case (k, v) =>
          k -> v.map(_.toString).getOrElse(baseYear.toString)
        },
        BuildInfoKey.map(startYear) { case (k, v) =>
          "copyright" -> s"© ${v.map(_.toString).getOrElse(baseYear.toString)}-${Calendar
              .getInstance()
              .get(Calendar.YEAR)} $orgName}"
        },
        BuildInfoKey.map(scalaVersion) { case (k, v) =>
          val version = if (v.head == '2') {
            v.substring(0, v.lastIndexOf('.'))
          } else v
          "scalaCompatVersion" -> version
        },
        BuildInfoKey.map(licenses) { case (k, v) =>
          k -> v.map(_._1).mkString(", ")
        }
      )
    )
  }

}
