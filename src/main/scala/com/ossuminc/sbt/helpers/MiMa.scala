package com.ossuminc.sbt.helpers

import com.typesafe.tools.mima.core.{Problem, ProblemFilters}
import sbt.*
import sbt.Keys.*
import sbt.Project
import com.typesafe.tools.mima.plugin.MimaKeys.*
import com.typesafe.tools.mima.plugin.MimaKeys.{mimaFailOnNoPrevious, mimaPreviousArtifacts}
import com.typesafe.tools.mima.plugin.MimaPlugin
import sbttastymima.TastyMiMaPlugin
import sbttastymima.TastyMiMaPlugin.autoImport.*

/** LightbendLabs Migration Manager support. This includes the sbt-mima-plugin to check for backwards binary
  * compatibility in Scala libraries.
  * @see
  *   https://index.scala-lang.org/lightbend-labs/mima
  */
object MiMa extends AutoPluginHelper {

  override def configure(project: Project) = Without(project)
  
  /** Mark a project as not processed by MiMa */
  def Without(project: Project): Project = {
    project
      .disablePlugins(MimaPlugin, TastyMiMaPlugin)
      .settings(
        mimaPreviousArtifacts := Set.empty,
        mimaFailOnNoPrevious := false
      )
  }

  /** Configure MiMa to operate on this project
    * @param previousVersion
    *   The mimaPreviousArtifacts setting that MiMa requires will be set to
    *   {{{organization %% moduleName % previousVersion}}} Make sure that your organization is set with the
    *   [[com.ossuminc.sbt.Root]] object correctly. The moduleName comes from the second argument to your
    *   [[com.ossuminc.sbt.Module]] or [[com.ossuminc.sbt.CrossModule]] invocation.
    * @param excludedClasses
    *   A list of classes to be excluded from Binary Issue checks. This can safely be done to prevent errors for
    *   internal classes that are not part of the project's API
    * @param reportSignatureIssues
    *   When set to `true`, this turns on the `mimaReportSignatureProblems` setting which compares the whole function
    *   signature, including generic type parameters.
    */
  def With(
    previousVersion: String,
    excludedClasses: Seq[String] = Seq.empty,
    reportSignatureIssues: Boolean = false
  )(project: Project): Project = {
    project
      .enablePlugins(TastyMiMaPlugin)
      .enablePlugins(MimaPlugin)
      .enablePlugins()
      .settings(
        mimaPreviousArtifacts := Set[ModuleID](organization.value %% moduleName.value % previousVersion),
        tastyMiMaPreviousArtifacts += { organization.value %% moduleName.value % previousVersion },
        mimaReportSignatureProblems := reportSignatureIssues,
        mimaBinaryIssueFilters ++= excludedClasses.map { className =>
          ProblemFilters.exclude[Problem](className)
        }
      )
  }
}
