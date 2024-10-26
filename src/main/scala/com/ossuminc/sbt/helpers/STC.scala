package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.{organization, streams}

import scala.sys.process.Process

/** An AutoPluginHelper to make using ScalablyTyped easier  */
object STC extends AutoPluginHelper {

  override def configure(project: Project): Project = run_stc(file("."))(project)

  lazy val runSTC = taskKey[File]("Runs ScalablyTyped Converter (stc)")

  def run_stc(
    packageJsonDir: File,
    useScalaJsDomTypes: Boolean = false,
    scalaJsVersion: String = "1.17.0",
    scalaVersion: String = "3.4.3",
    outputPackage: String = "org.ossum.sauce",
    stdlib: String = "",
    ignoredLibs: String = "",
    libsToTranslate: Seq[String] = Seq.empty[String]
  )(project: Project): Project = {
    val package_json_dir = packageJsonDir.getAbsoluteFile
    val command_prefix = "stc " +
      "--directory " + package_json_dir + " " +
      "--useScalaJsDomTypes " + { if (useScalaJsDomTypes) "true " else "false " } +
      s"--scalajs $scalaJsVersion " +
      s"--scala $scalaVersion " +
      s"--outputPackage $outputPackage " +
      s"--stdlib $stdlib " + {
      if (ignoredLibs.isEmpty) "" else s"--ignoredLibs $ignoredLibs "
    }


    project
      .settings(
        runSTC := {
          val command = command_prefix + s"--organization ${organization.value} --ignoredLibs $ignoredLibs " +
            libsToTranslate.mkString(" ")
          val log = streams.value.log
          log.info(s"Converting Typescript packages to Scala.js facades in ${packageJsonDir.toString}")
          val rc = Process(command, package_json_dir).!
          log.info(s"Conversion completed: rc == $rc")
          packageJsonDir
        }
      )
  }
}

// Usage: stc [options] [libs]
//
//  -h, --help
//  -v, --version
//  -d, --directory <value>  Specify another directory instead of the current directory where your package.json and node_modules is
//  --includeDev <value>     Include dev dependencies
//  --includeProject <value>
//                           Include project in current directory
//  --useScalaJsDomTypes <value>
//                           When true (which is the default) uses scala-js-dom types when possible instead of types we translate from typescript in std
//  -f, --flavour <value>    One of normal, japgolly, slinky, slinky-native. See https://scalablytyped.org/docs/flavour
//  --scalajs <value>        Scala.js version
//  --scala <value>          Scala version
//  --outputPackage <value>  Output package
//  --enableScalaJSDefined <value>
//                           Libraries you want to enable @ScalaJSDefined traits for.
//  -s, --stdlib <value>     Which parts of typescript stdlib you want to enable
//  --organization <value>   Organization used (locally) publish artifacts
//  --ignoredLibs <value>    Libraries you want to ignore
//  libs                     Libraries you want to convert from node_modules

