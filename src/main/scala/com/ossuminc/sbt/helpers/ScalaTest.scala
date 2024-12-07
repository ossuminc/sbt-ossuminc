package com.ossuminc.sbt.helpers
import sbt.*
import sbt.Keys.libraryDependencies
import sbt.Project
import sbt.librarymanagement.ModuleID
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*

object ScalaTest extends AutoPluginHelper {

  override def configure(project: Project): Project = { configure()(project) }

  def configure(
    version: String = "3.2.19",
    scalacheckVersion: Option[String] = None,
    nonJVM: Boolean = true
  )(project: Project): Project = {

    project.settings(
      libraryDependencies ++= {
        def scalactic(version: String, nonJVM: Boolean): ModuleID = {
          if (nonJVM) "org.scalactic" %%% "scalactic" % version % Test
          else "org.scalactic" %% "scalactic" % version % Test
        }
        def scalatest(version: String, nonJVM: Boolean): ModuleID = {
          if (nonJVM) "org.scalatest" %%% "scalatest" % version % Test
          else "org.scalatest" %% "scalatest" % version % Test
        }
        def scalacheck(version: String, nonJVM: Boolean): ModuleID = {
          if (nonJVM) "org.scalacheck" %%% "scalacheck" % version % Test
          else "org.scalacheck" %% "scalacheck" % version % Test
        }

        val some = Seq[ModuleID](scalactic(version, nonJVM), scalatest(version, nonJVM))
        val maybe = scalacheckVersion match {
          case Some(version) => Seq[ModuleID](scalacheck(version, nonJVM))
          case None          => Seq.empty[ModuleID]
        }
        some ++ maybe
      }
    )
  }
}
