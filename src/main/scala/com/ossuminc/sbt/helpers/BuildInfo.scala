package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*
import sbtbuildinfo.BuildInfoKeys.buildInfoUsePackageAsPath
import sbtbuildinfo.BuildInfoOption.{BuildTime, ConstantValue, ToJson, ToMap}
import sbtbuildinfo.BuildInfoPlugin.autoImport.*
import sbtbuildinfo.{BuildInfoKey, BuildInfoPlugin}

import java.util.Calendar
import scala.language.implicitConversions // SettingKey/TaskKey -> BuildInfoKey (Scala 3)

object BuildInfo extends AutoPluginHelper {

  /** The configuration function to call for this plugin helper
    *
    * @param project
    *   The project to which the configuration should be applied
    *
    * @return
    *   The same project passed as an argument, post configuration
    */
  def apply(project: Project): Project = {
    val utcDate: String = java.time.Instant
      .now()
      .atZone(java.time.ZoneOffset.UTC)
      .format(java.time.format.DateTimeFormatter.ISO_INSTANT);
    project
      .enablePlugins(BuildInfoPlugin)
      .settings(
        buildInfoObject := "BuildInfo",
        buildInfoPackage := "com.ossuminc",
        buildInfoOptions ++= Seq(ToMap, ToJson, BuildTime, ConstantValue),
        buildInfoUsePackageAsPath := true,
        buildInfoKeys ++= Seq[BuildInfoKey](
          BuildInfoKey(normalizedName),
          BuildInfoKey(moduleName),
          BuildInfoKey(description),
          BuildInfoKey(organization),
          BuildInfoKey(organizationName),
          BuildInfoKey(RootProjectInfo.Keys.gitHubOrganization),
          BuildInfoKey(RootProjectInfo.Keys.gitHubRepository),
          BuildInfoKey(RootProjectInfo.Keys.copyrightHolder),
          BuildInfoKey.map(BuildInfoKey(organizationHomepage)) { case (k, v) =>
            k -> v.get.toString
          },
          BuildInfoKey.map(BuildInfoKey(homepage)) { case (_, v) =>
            "projectHomepage" -> v
              .map(_.toString)
              .getOrElse(RootProjectInfo.Keys.projectHomePage.value.toString)
          },
          BuildInfoKey.map(BuildInfoKey(licenses)) { case (k, v) =>
            k -> v.map(_.toString).mkString(", ")
          },
          BuildInfoKey(isSnapshot),
          BuildInfoKey(buildInfoPackage),
          BuildInfoKey(buildInfoObject),
          BuildInfoKey.map(BuildInfoKey(startYear)) { case (k, v) =>
            k -> v.map(_.toString).getOrElse(RootProjectInfo.Keys.projectStartYear.value.toString)
          },
          BuildInfoKey.map(BuildInfoKey(startYear)) { case (_, v) =>
            "copyright" -> s"© ${v.map(_.toString).getOrElse(RootProjectInfo.Keys.projectStartYear.value.toString)}-${Calendar
                .getInstance()
                .get(Calendar.YEAR)} ${organizationName.value}"
          },
          BuildInfoKey.map(BuildInfoKey(scalaVersion)) { case (_, v) =>
            val version = if (v.head == '2') {
              v.substring(0, v.lastIndexOf('.'))
            } else v
            "scalaCompatVersion" -> version
          },
          BuildInfoKey("buildInstant" -> utcDate)
        )
      )
  }

  /** Configure with additional key/value pairs provided by first argument
    *
    * @param keyValues
    *   The keys and value to add to the BuildInfo output
    */
  def withKeys(keyValues: (String, Any)*)(project: Project): Project = {
    apply(project).settings(
      buildInfoKeys ++= { keyValues.map { case (k, v) => BuildInfoKey.action(k) { v } } }
    )
  }
}
