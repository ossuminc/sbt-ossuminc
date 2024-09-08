package com.ossuminc.sbt.helpers

import com.typesafe.tools.mima.core.{Problem, ProblemFilters}
import sbt.*
import sbt.Keys.*
import sbt.Project
import com.typesafe.tools.mima.plugin.MimaKeys.*
import com.typesafe.tools.mima.plugin.MimaKeys.{mimaFailOnNoPrevious, mimaPreviousArtifacts}
import sbttastymima.TastyMiMaPlugin.autoImport.*

/** LightbendLabs Migration Manager support. This includes the sbt-mima-plugin to check for
 * backwards binary compatibility in Scala libraries.
 * @see https://index.scala-lang.org/lightbend-labs/mima
 * */
object MiMa extends AutoPluginHelper {

  /** Mark a project as not processed by MiMa */
  def configure(project: Project): Project = {
    project.settings(
      mimaFailOnNoPrevious := false
    )
  }

  /** Configure MiMa to operate on this project
   * @param previousVersion
   *   The mimaPreviousArtifacts setting that MiMa requires will be set to
   *   {{{organization %% moduleName % previousVersion}}}
   *   Make sure that your organization is set with the [[com.ossuminc.sbt.Root]] object
   *   correctly. The moduleName comes from the second argument to your [[com.ossuminc.sbt.Module]] or
   *   [[com.ossuminc.sbt.CrossModule]] invocation.
   * @param excludedClasses
   *   A list of classes to be excluded from Binary Issue checks. This can safely be done to prevent
   *   errors for internal classes that are not part of the project's API
   * @param reportSignatureIssues
   *   When set to `true`, this turns on the `mimaReportSignatureProblems` setting which compares the
   *   whole function signature, including generic type parameters.
   * */
  def configure(
   previousVersion: String,
   excludedClasses: Seq[String] = Seq.empty,
   reportSignatureIssues: Boolean = false
  )(project: Project): Project = {
    project.settings(
      mimaPreviousArtifacts := Set(organization.value %% moduleName.value % previousVersion),
      tastyMiMaPreviousArtifacts += { organization.value %% moduleName.value % previousVersion },
      mimaReportSignatureProblems := reportSignatureIssues,
      mimaBinaryIssueFilters ++= excludedClasses.map { className =>
        ProblemFilters.exclude[Problem](className)
      }
    )
  }
}
