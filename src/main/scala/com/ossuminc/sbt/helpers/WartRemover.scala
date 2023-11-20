package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*
import wartremover.{Wart, Warts}
import wartremover.WartRemover.autoImport._

object WartRemover extends AutoPluginHelper {

  object Keys {
    val excludedWarts: SettingKey[Seq[Wart]] = settingKey[Seq[Wart]](
      "Warts to exclude from WartRemover"
    )
  }

  override def autoPlugins: Seq[AutoPlugin] = Seq(wartremover.WartRemover)

  private def filteredWarts(excluded: Seq[Wart]): Seq[Wart] = {
    Warts.all.filterNot { wart => excluded.contains(wart) }
  }

  def configure(project: Project): Project = {
    project
      .settings(
        Keys.excludedWarts := Seq.empty[Wart],
        Compile / compile / wartremoverWarnings := filteredWarts(Keys.excludedWarts.value),
        Test / compile / wartremoverWarnings := Seq.empty[Wart],
        wartremoverExcluded := {
          val scalaVer: String = project / scalaVersion.value
          Seq(project.base / "target" / s"scala-$scalaVer" / "src_managed" / "main")
        }
      )
  }
}
