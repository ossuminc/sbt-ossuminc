package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import org.scalajs.linker.interface.ModuleSplitStyle
import com.github.sbt.git.GitPlugin.autoImport.git

object ScalaJS extends AutoPluginHelper {

  /** Default version for scala-java-time dependency */
  val defaultScalaJavaTimeVersion = "2.6.0"

  /** Default version for scalatest dependencies */
  val defaultScalatestVersion = "3.2.19"

  override def apply(project: Project): Project = apply()(project)

  /** Configure Scala.js compilation
    *
    * @param header JS header comment for output files
    * @param hasMain Whether the project has a main method
    * @param forProd Whether to optimize for production
    * @param withCommonJSModule Whether to use CommonJS modules (vs ES modules)
    * @param scalaJavaTimeVersion Version of scala-java-time to include
    * @param scalatestVersion Version of scalatest to include for testing
    */
  def apply(
    header: String = "no header",
    hasMain: Boolean = false,
    forProd: Boolean = true,
    withCommonJSModule: Boolean = false,
    scalaJavaTimeVersion: String = defaultScalaJavaTimeVersion,
    scalatestVersion: String = defaultScalatestVersion
  )(
    project: Project
  ): Project = {
    project
      .enablePlugins(ScalaJSPlugin)
      // .settings(ScalaJSPlugin.testConfigSettings) <-- generates undefined settings
      .settings(
        // For source maps in Scala - only if git commit and scmInfo are available
        scalacOptions ++= {
          (git.gitHeadCommit.value, scmInfo.value) match {
            case (Some(headCommit), Some(info)) =>
              // Map the sourcemaps to github paths instead of local directories
              val flag =
                if (scalaVersion.value.startsWith("3")) "-scalajs-mapSourceURI"
                else "-P:scalajs:mapSourceURI"
              val localSourcesPath = baseDirectory.value.toURI
              val remoteSourcesPath =
                s"${
                  info.browseUrl.toString
                    .replace("github.com", "raw.githubusercontent.com")
                }/$headCommit"
              Seq(s"${flag}:$localSourcesPath->$remoteSourcesPath")
            case _ =>
              // No git info or scmInfo available - skip source map configuration
              Seq.empty
          }
        },
        // for an application with a main method
        scalaJSUseMainModuleInitializer := hasMain,
        scalaJSLinkerConfig ~= {
          _.withOptimizer(forProd)
            .withModuleKind({
              if (withCommonJSModule) {
                ModuleKind.CommonJSModule
              } else {
                ModuleKind.ESModule
              }
            })
            .withModuleSplitStyle(ModuleSplitStyle.FewestModules)
            .withSourceMap(!forProd)
            .withJSHeader("// " + header + "\n")
        },
        libraryDependencies ++= Seq(
          "io.github.cquiroz" %%% "scala-java-time" % scalaJavaTimeVersion,
          "org.scalactic" %%% "scalactic" % scalatestVersion % "test",
          "org.scalatest" %%% "scalatest" % scalatestVersion % "test",
          "org.scalatest" %%% "scalatest-funspec" % scalatestVersion % "test"
        )
      )

  }
}
