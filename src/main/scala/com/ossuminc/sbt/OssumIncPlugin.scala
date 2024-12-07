package com.ossuminc.sbt

import com.ossuminc.sbt.helpers.Packaging
import sbt.*
import sbt.Keys.*
import sbt.librarymanagement.Resolver

/** And sbt plugin for many different kind of projects and used for every project at Ossum Inc.
  */
object OssumIncPlugin extends AutoPlugin {

  object autoImport {
    // The type of function that `Project.configure` takes as its argument
    private type ConfigFunc = Project => Project

    // Major declarations
    val Root: com.ossuminc.sbt.Root.type = com.ossuminc.sbt.Root
    val Module: com.ossuminc.sbt.Module.type = com.ossuminc.sbt.Module
    val Plugin: com.ossuminc.sbt.Plugin.type = com.ossuminc.sbt.Plugin
    val Program: com.ossuminc.sbt.Program.type = com.ossuminc.sbt.Program
    val CrossModule: com.ossuminc.sbt.CrossModule.type = com.ossuminc.sbt.CrossModule
    val DocSite: com.ossuminc.sbt.DocSite.type = com.ossuminc.sbt.DocSite
    val JVM: CrossModule.Target = CrossModule.JVMTarget
    val JS: CrossModule.Target = CrossModule.JSTarget
    val Native: CrossModule.Target = CrossModule.NativeTarget

    // Clauses to customize the major declarations
    object With {

      private def these(cfuncs: ConfigFunc*)(project: Project): Project =
        cfuncs.foldLeft(project) { (p, func) =>
          p.configure(func)
        }

      /** Use this to provide dependencies on most recent Akka libraries */
      val akka: ConfigFunc = helpers.Akka.configure

      /** Use this to provide handy sbt command line aliases */
      val aliases: ConfigFunc = helpers.HandyAliases.configure

      /** Use this to have the build generate build information. "I know this because sbt knows this" */
      val build_info: ConfigFunc = helpers.BuildInfo.configure

      /** Configure the project to require a certain percentage of coverage in test cases */
      def coverage(percent: Double = 50.0d)(project: Project): Project =
        project
          .configure(helpers.ScalaCoverage.configure)
          .settings(
            helpers.ScalaCoverage.Keys.coveragePercent := percent
          )

      /** Use dynamic versioning based on the most recent tag, and the commit hash and data/time stamp if necessary */
      val dynver: ConfigFunc = helpers.DynamicVersioning.configure

      /** Use this to get git command line support at the sbt prompt */
      val git: ConfigFunc = helpers.Git.configure

      /** Use this to get the `headerCheck` and `headerCreate` sbt commands to generate source file headers
       * automatically
       */
      val header: ConfigFunc = helpers.Header.configure

      /** Use this to provide th spdx license code for header license */
      def headerLicense(spdx: String)(project: Project): Project =
        helpers.Header.specificLicense(spdx)(project)

      /** Use this to enable compilation of Java code too */
      val java: ConfigFunc = helpers.Java.configure

      /** Use this to configure your project to compile Scala to Javascript via scala.js */
      def js(
              header: String = "no header",
              hasMain: Boolean = false,
              forProd: Boolean = true,
              withCommonJSModule: Boolean = false
            )(project: Project): Project =
        helpers.Javascript.configure(header, hasMain, forProd, withCommonJSModule)(project)

      /** Use this to configure your project to include typical laminar dependencies */
      def laminar(version: String = "17.1.0", domVersion: String = "2.8.0")(project: Project): Project =
        helpers.Laminar.configure(version, domVersion)(project)

      /** Use this to configure your project to compile to native code The defaults are usually sufficient but the
       * arguments to this function make it easy to specify the options that ScalaNative provides.
       *
       * @param mode
       * Choose from "debug", "fast", "full", "size". Default is "debug"
       * @param buildTarget
       * Choose from "static", "dynamic" or "application". Default is "static"
       * @param gc
       * Choose from "immix", "commix", "boehm", or "none". Default is "commix"
       * @param lto
       * Choos from "none", "full", "thin". Default is "none"  
       * @param debugLog
       * Enable for debug logging from Scala Native
       * @param verbose
       * Enable for verbose output from Scala Native 
       * @param linkOptions
       * Pass additional linker options 
       * @param project
       * The project to be configured
       * @return
       * The configured project
       */
      def native(
                  mode: String = "fast",
                  buildTarget: String = "debug",
                  gc: String = "boehm",
                  lto: String = "none",
                  debugLog: Boolean = false,
                  verbose: Boolean = false,
                  targetTriple: Option[String] = None,
                  linkOptions: Seq[String] = Seq.empty
                )(project: Project): Project =
        helpers.Native.configure(mode, buildTarget, lto, gc, debugLog, verbose, targetTriple, linkOptions)(project)

      /** Configure Lightbend's Migration Manager for compatibility checking */
      def MiMa(
                previousVersion: String,
                excludedClasses: Seq[String] = Seq.empty,
                reportSignatureIssues: Boolean = false
              )(project: Project): Project =
        helpers.MiMa.configure(previousVersion, excludedClasses, reportSignatureIssues)(project)

      /** Do not configure this project for Lightbend's Migration Manager */
      val noMiMa: ConfigFunc = helpers.MiMa.configure

      /** Configure your project to package using the sbt-native-packager Universal mode.
       *
       * @param maintainerEmail
       * Email address of the maintainer of the package
       * @param pkgName
       * Name of the package
       * @param pkgSummary
       * Brief summary of the package
       * @param pkgDescription
       * Longer description of the package
       * @param project
       * The project to configure
       * @return
       * The configure project
       */
      def packagingUniversal(
                              maintainerEmail: String,
                              pkgName: String,
                              pkgSummary: String,
                              pkgDescription: String
                            )(project: Project): Project =
        Packaging.universal(maintainerEmail, pkgName, pkgSummary, pkgDescription)(project)

      /** Configure your project to package using the sbt-native-packager Docker mode.
       *
       * @param maintainerEmail
       * Email address of the maintainer of the package
       * @param pkgName
       * Name of the package
       * @param pkgSummary
       * Brief summary of the package
       * @param pkgDescription
       * Longer description of the package
       * @param project
       * The project to configure
       * @return
       * The configure project
       */
      def packagingDocker(
                           maintainerEmail: String,
                           pkgName: String,
                           pkgSummary: String,
                           pkgDescription: String
                         )(project: Project): Project =
        Packaging.docker(maintainerEmail, pkgName, pkgSummary, pkgDescription)(project)

      /** Configure your project to generate a sbt plugin */
      def plugin(project: Project): Project =
        project
          .configure(scala2)
          .settings(
            scalaVersion := "2.12.19",
            sbtPlugin := true
          )

      /** Configure this project to be published as open source */
      val publishing: ConfigFunc = helpers.SonatypePublishing.configure

      /** Configure this project to produce no artifact and not be published */
      def noPublishing(project: Project): Project =
        project.settings(
          publishArtifact := false, // no artifact to publish for the virtual root project
          publish := {}, // just to be sure
          publishLocal := {}, // and paranoid
          publishTo := Some(Resolver.defaultLocal),
          publish / skip := true
        )

      /** Configure this project to support releasing with a systematic release procedure */
      val release: ConfigFunc = helpers.Release.configure

      /** Add extra resolvers to the build for this project */
      val resolvers: ConfigFunc = helpers.Resolvers.configure

      /** Configure dependency on a version of the RIDDL library */
      def riddl(version: String, nonJVM: Boolean = true)(project: Project): Project =
        project.configure(helpers.Riddl.configure(version, nonJVM))

      def riddlTestKit(version: String, nonJVM: Boolean = true)(project: Project): Project =
        project.configure(helpers.Riddl.testKit(version, nonJVM))

      /** Compile scala code as Scala 2.13.latest */
      val scala2: ConfigFunc = helpers.Scala2.configure

      /** Compile scala code as Scala 3's latest LTS release */
      val scala3: ConfigFunc = helpers.Scala3.configure

      /** Configure this project to use standard Scalafmt formatting rules. */
      val scalafmt: ConfigFunc = helpers.Scalafmt.configure

      /** Add scalaTest libraries to the libraryDependencies */
      def scalaTest(
                     version: String = "3.2.19",
                     scalaCheckVersion: Option[String] = None,
                     nonJVM: Boolean = false
                   )(project: Project): Project =
        helpers.ScalaTest.configure(version, scalaCheckVersion, nonJVM)(project)

      /** Configure this project to enable coverage testing */
      val scoverage: ConfigFunc = helpers.ScalaCoverage.configure

      /** Configure ScalablyTyped/Converter to generate Scala.js facades for a set of Typescript dependencies that are
       * loaded using `scalajs-bundler`. If you don't want to use `scalajs-bundler`, use
       *
       * @see
       * https://scalablytyped.org/docs/plugin#how-it-works
       * @see
       * https://scalablytyped.org/docs/usage
       * @param dependencies
       * The list of TypeScript dependencies from NPM that you want to convert. This should be a Map value similar to
       * the "dependencies" item from `package.json`:
       * {{{
       *      Map(
       *        "react-router-dom" -> "5.1.2",
       *        "@types/react-router-dom" -> "5.1.2"
       *      )
       *     }}}
       * Note that some packages contain first-party typescript type definitions, while for others like
       * react-router-dom we need to get separate @types packages. These are originally from DefinitelyTyped. This
       * parameter is required.
       * @param useNPM
       * Whether to use NPM or Yarn. This parameter is optional and defaults to `true`. This helper checks for
       * updated npm dependencies on each compile, and yarn responds much faster than npm. Yarn will need to be
       * present on your system for this to work. You should also check in yarn.lock.
       * @param useScalaJsDom
       * @see
       * https://scalablytyped.org/docs/conversion-options#stusescalajsdom
       * @param minimizeAllTransitives
       * When set to true, all transitive dependencies will be minimized. Otherwise, none will be. Exceptions to this
       * rule can be made with the `exceptions` parameter. Default: true (minimize all transitive dependencies)
       * @param exceptions
       * A list of exceptions to the "All" or "None" approach for `allTransitives` parameter. Default value is
       * List.empty()
       * @see
       * https://scalablytyped.org/docs/library-developer#compiling-all-that-generated-code
       * @param ignore
       * A list of transitive dependencies to ignore (i.e. do not generate Scala.js facades for them) Default value
       * is List.empty()
       * @see
       * https://scalablytyped.org/docs/conversion-options#stignore
       * @param outputPackage
       * The name of the scala package to which you want the Scala.js facades generated Default value:
       * "org.ossum.sauce"
       * @param withDebugOutput
       * Turn on verbose debug output. Default is false
       * @return
       * Project
       */
      def scalablyTypedWithScalaJsBundler(
                                           dependencies: Map[String, String],
                                           useNPM: Boolean = true,
                                           useScalaJsDom: Boolean = false,
                                           minimizeAllTransitives: Boolean = true,
                                           exceptions: List[String] = List.empty[String],
                                           ignore: List[String] = List.empty[String],
                                           outputPackage: String = "org.ossum.sauce",
                                           withDebugOutput: Boolean = false
                                         )(project: Project): Project =
        helpers.ScalablyTyped.withScalajsBundler(
          dependencies,
          useNPM,
          useScalaJsDom,
          minimizeAllTransitives,
          exceptions,
          ignore,
          outputPackage,
          withDebugOutput
        )(project)
  
      /** Configure ScalablyTyped/Converter to generate Scala.js facades for a set of Typescript dependencies without
       * using `scalajs-bundler`. If you want to use `scalajs-bundler`, use the `With.scalablyTypedWithScalaJsBundler`
       * helper.
       *
       * @see
       * https://scalablytyped.org/docs/plugin#how-it-works
       * @see
       * https://scalablytyped.org/docs/usage
       * @param packageJsonDir
       * The directory containing the `package.json` file from which dependencies will be processed by ScalablyTyped.
       * This parameter is required.
       * @param useScalaJsDom
       * @see
       * https://scalablytyped.org/docs/conversion-options#stusescalajsdom
       * @param minimizeAllTransitives
       * When set to true, all transitive dependencies will be minimized. Otherwise, none will be. Exceptions to this
       * rule can be made with the `exceptions` parameter. Default: true (minimize all transitive dependencies)
       * @param exceptions
       * A list of exceptions to the "All" or "None" approach for `allTransitives` parameter. Default value is
       * List.empty()
       * @see
       * https://scalablytyped.org/docs/library-developer#compiling-all-that-generated-code
       * @param ignore
       * A list of transitive dependencies to ignore (i.e. do not generate Scala.js facades for them) Default value
       * is List.empty()
       * @see
       * https://scalablytyped.org/docs/conversion-options#stignore
       * @param outputPackage
       * The name of the scala package to which you want the Scala.js facades generated Default value:
       * "org.ossum.sauce"
       * @param withDebugOutput
       * Turn on verbose debug output. Default is false
       * @return
       * Project
       */
      def scalablyTyped(
                         packageJsonDir: File,
                         useScalaJsDom: Boolean = false,
                         minimizeAllTransitives: Boolean = true,
                         exceptions: List[String] = List.empty[String],
                         ignore: List[String] = List.empty[String],
                         outputPackage: String = "org.ossum.sauce",
                         withDebugOutput: Boolean = false
                       )(project: Project): Project =
        helpers.ScalablyTyped.withoutScalajsBundler(
          packageJsonDir,
          useScalaJsDom,
          minimizeAllTransitives,
          exceptions,
          ignore,
          outputPackage,
          withDebugOutput
        )(project)
  
      /** Use this to enable and configure the sbt-unidoc plugin in your project */
      def unidoc(
                  apiOutput: File = file("target/unidoc"),
                  baseURL: Option[String] = None,
                  inclusions: Seq[ProjectReference] = Seq.empty,
                  exclusions: Seq[ProjectReference] = Seq.empty,
                  logoPath: Option[String] = None,
                  externalMappings: Seq[Seq[String]] = Seq.empty
                )(project: Project): Project =
        project
          .configure(helpers.Unidoc.configure(apiOutput, baseURL, inclusions, exclusions, logoPath, externalMappings))
  
      /** Use this to more easily configure:
       *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.aliases]],
       *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.dynver]]
       *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.git]]
       *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.header]]
       *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.resolvers]] more easily
       */
      def basic(project: Project): Project =
        these(aliases, dynver, git, header, resolvers)(project)
  
      /** Configure support for all the simple things:
       *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.typical]]
       *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.java]]
       *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.build_info]]
       *   - [[com.ossuminc.sbt.OssumIncPlugin.autoImport.With.release]]
       */
      def everything(project: Project): Project = {
        project.configure(typical)
        these(java, build_info, release)(project)
      }

  
      /** Use this to enable the [[basic]] features as well as [[scala3]] and [[publishing]] */
      def typical(project: Project): Project = {
        project.configure(basic)
        these(scala3, publishing)(project)
      }
    }
  }

  override def projectSettings: Seq[Setting[_]] = Nil
}
