package com.ossuminc.sbt.helpers
import sbt.*
import sbt.Keys.libraryDependencies
import sbt.Project
import sbt.librarymanagement.ModuleID

/** Scalatest testing library support.
  *
  * @note In sbt 2.x, cross-platform (nonJVM=true) is not available until
  *       sbt-platform-deps supports sbt 2.0. Use nonJVM=false for JVM-only.
  */
object Scalatest extends AutoPluginHelper {

  val latest_version = "3.2.19"

  override def apply(project: Project): Project = apply()(project)

  def apply(
    version: String = "3.2.19",
    scalacheckVersion: Option[String] = None,
    nonJVM: Boolean = false
  )(project: Project): Project = {
    if (nonJVM) {
      throw new UnsupportedOperationException(
        "Scalatest with nonJVM=true is not available in sbt 2.x. " +
          "The sbt-platform-deps plugin (required for %%%) does not yet support sbt 2.0. " +
          "Use nonJVM=false for JVM-only builds."
      )
    }
    project.settings(
      libraryDependencies ++= {
        def scalactic(version: String): ModuleID =
          "org.scalactic" %% "scalactic" % version % Test
        def scalatest(version: String): ModuleID =
          "org.scalatest" %% "scalatest" % version % Test
        def scalacheck(version: String): ModuleID =
          "org.scalacheck" %% "scalacheck" % version % Test

        val some = Seq[ModuleID](scalactic(version), scalatest(version))
        val maybe = scalacheckVersion match {
          case Some(v) => Seq[ModuleID](scalacheck(v))
          case None    => Seq.empty[ModuleID]
        }
        some ++ maybe
      }
    )
  }
}
