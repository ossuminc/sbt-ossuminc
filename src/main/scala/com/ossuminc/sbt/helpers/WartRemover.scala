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

  def configure(project: Project): Project = {
    project
      .enablePlugins(wartremover.WartRemover)
      .settings(
        Keys.excludedWarts := Seq.empty[Wart],
        Compile / compile / wartremoverWarnings :=
          Warts.all.filterNot { wart => Keys.excludedWarts.value.contains(wart) },
        Test / compile / wartremoverWarnings := Seq.empty[Wart],
        wartremoverExcluded := {
          val scalaVer: String = scalaVersion.value
          val sv = if (scalaVer.startsWith("3")) "3" else scalaVer.take(4)
          Seq(project.base / "target" / s"scala-$sv" / "src_managed" / "main")
        }
      )
  }
}
