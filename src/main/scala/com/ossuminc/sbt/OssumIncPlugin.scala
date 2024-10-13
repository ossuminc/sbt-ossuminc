package com.ossuminc.sbt

import sbt.*
import sbt.Keys.*
import sbt.librarymanagement.Resolver

/** And sbt plugin for many different kind of projects and used for every project at Ossum Inc.
 *  */
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
      val akka: ConfigFunc = helpers.Akka.configure
      val aliases: ConfigFunc = helpers.HandyAliases.configure
      val build_info: ConfigFunc = helpers.BuildInfo.configure
      val dynver: ConfigFunc = helpers.DynamicVersioning.configure
      val git: ConfigFunc = helpers.Git.configure
      val header: ConfigFunc = helpers.Header.configure
      val java: ConfigFunc = helpers.Java.configure
      val javascript: ConfigFunc = helpers.Javascript.configure
      val misc: ConfigFunc = helpers.Miscellaneous.configure
      val native: ConfigFunc = helpers.Native.configure
      val noMiMa: ConfigFunc = helpers.MiMa.configure
      val publishing: ConfigFunc = helpers.SonatypePublishing.configure
      val release: ConfigFunc = helpers.Release.configure
      val resolvers: ConfigFunc = helpers.Resolvers.configure
      val scala2: ConfigFunc = helpers.Scala2.configure
      val scala3: ConfigFunc = helpers.Scala3.configure
      val scalaTest: ConfigFunc = helpers.ScalaTest.configure
      val scalafmt: ConfigFunc = helpers.Scalafmt.configure
      val scoverage: ConfigFunc = helpers.ScalaCoverage.configure

      def these(cfuncs: ConfigFunc*)(project: Project): Project = {
        cfuncs.foldLeft(project) { (p, func) =>
          p.configure(func)
        }
      }

      def basic(project: Project): Project = {
        these(aliases, dynver, git, header, resolvers)(project)
      }

      def typical(project: Project): Project = {
        project.configure(basic)
        these(scala3, scalaTest, publishing)(project)
      }

      def everything(project: Project): Project = {
        project.configure(typical)
        these(java, misc, build_info, release)(project)
      }

      def unidoc(
        apiOutput: File = file("target/unidoc"),
        baseURL: Option[String] = None,
        inclusions: Seq[ProjectReference] = Seq.empty,
        exclusions: Seq[ProjectReference] = Seq.empty,
        logoPath: Option[String] = None,
        externalMappings: Seq[Seq[String]] = Seq.empty
      )(project: Project): Project = {
        project
          .configure(helpers.Unidoc.configure(apiOutput, baseURL, inclusions,exclusions, logoPath, externalMappings))
      }


      def noPublishing(project: Project): Project = {
        project.settings(
          publishArtifact := false, // no artifact to publish for the virtual root project
          publish := {}, // just to be sure
          publishLocal := {}, // and paranoid
          publishTo := Some(Resolver.defaultLocal),
          publish / skip := true
        )
      }

      def coverage(percent: Double = 50.0d)(project: Project): Project = {
        project
          .configure(helpers.ScalaCoverage.configure)
          .settings(
            helpers.ScalaCoverage.Keys.coveragePercent := percent
          )
      }

      def js(
        header: String = "no header",
        hasMain: Boolean = false,
        forProd: Boolean = true,
        withCommonJSModule: Boolean = false
      )(project: Project): Project = {
        helpers.Javascript.configure(header, hasMain, forProd, withCommonJSModule)(project)
      }

      def laminar(version: String = "17.1.0", domVersion: String = "2.8.0")(project: Project): Project =
        helpers.Laminar.configure(version, domVersion)(project)

      def MiMa(
        previousVersion: String,
        excludedClasses: Seq[String] = Seq.empty,
        reportSignatureIssues: Boolean = false
      )(project:Project): Project =
        helpers.MiMa.configure(previousVersion, excludedClasses, reportSignatureIssues)(project)

      def native(
        mode: String = "debug",
        lto: String = "full",
        gc: String = "none",
        buildTarget : String = "static",
        debugLog: Boolean = false,
        verbose: Boolean = false,
        targetTriple: String = "arm64-apple-macosx11.0.0",
        ld64Path: String = "/opt/homebrew/opt/llvm/bin/ld64.lld"
      )(project: Project): Project = {
        helpers.Native.configure(
          mode, lto, gc, buildTarget, debugLog, verbose, targetTriple, ld64Path
        )(project)
      }

      def riddl(version: String)(project:Project):Project = {
        project.configure(helpers.Riddl.configure(version))
      }

      def plugin(project: Project): Project = {
        project
          .configure(scala2)
          .settings(
            scalaVersion := "2.12.19",
            sbtPlugin := true
          )
      }

      /** Configure ScalablyTyped/Converter to generate Scala.js facades for a set of Typescript dependencies
       * that are loaded using `scalajs-bundler`. If you don't want to use `scalajs-bundler`, use
       *
       * @see https://scalablytyped.org/docs/plugin#how-it-works
       * @see https://scalablytyped.org/docs/usage
       * @param dependencies
       *   The list of TypeScript dependencies from NPM that you want to convert. This should be a Map value
       *   similar to the "dependencies" item from `package.json`:
       *   {{{
       *      Map(
       *        "react-router-dom" -> "5.1.2",
       *        "@types/react-router-dom" -> "5.1.2"
       *      )
       *   }}}
       *   Note that some packages contain first-party typescript type definitions, while for others
       *   like react-router-dom we need to get separate @types packages. These are originally from
       *   DefinitelyTyped.
       *   This parameter is required.
       * @param useNPM
       *   Whether to use NPM or Yarn. This parameter is optional and defaults to `true`.
       *   This helper checks for updated npm dependencies on each compile, and yarn responds much faster than npm.
       *   Yarn will need to be present on your system for this to work. You should also check in yarn.lock.
       * @param useScalaJsDom
       *   @see https://scalablytyped.org/docs/conversion-options#stusescalajsdom
       * @param minimizeAllTransitives
       *   When set to true, all transitive dependencies will be minimized. Otherwise, none will be. Exceptions
       *   to this rule can be made with the `exceptions` parameter.
       *   Default: true (minimize all transitive dependencies)
       * @param exceptions
       *   A list of exceptions to the "All" or "None" approach for `allTransitives` parameter.
       *   Default value is List.empty()
       * @see https://scalablytyped.org/docs/library-developer#compiling-all-that-generated-code
       * @param ignore
       *  A list of transitive dependencies to ignore (i.e. do not generate Scala.js facades for them)
       *  Default value is List.empty()
       * @see https://scalablytyped.org/docs/conversion-options#stignore
       * @param outputPackage
       *  The name of the scala package to which you want the Scala.js facades generated
       *  Default value: "org.ossum.sauce"
       * @param withDebugOutput
       *  Turn on verbose debug output. Default is false
       * @return Project
       */
      def scalablyTypedWithScalaJsBundler(
        dependencies: Map[String,String],
        useNPM: Boolean = true,
        useScalaJsDom: Boolean = false,
        minimizeAllTransitives: Boolean = true,
        exceptions: List[String] = List.empty[String],
        ignore: List[String] = List.empty[String],
        outputPackage: String = "org.ossum.sauce",
        withDebugOutput: Boolean = false
      )(project: Project): Project = {
        helpers.ScalablyTyped.withScalajsBundler(
          dependencies, useNPM, useScalaJsDom, minimizeAllTransitives, exceptions, ignore, outputPackage,
          withDebugOutput
        )(project)
      }

      /** Configure ScalablyTyped/Converter to generate Scala.js facades for a set of Typescript dependencies
       * without using `scalajs-bundler`. If you want to use `scalajs-bundler`, use
       * the `With.scalablyTypedWithScalaJsBundler` helper.
       *
       * @see https://scalablytyped.org/docs/plugin#how-it-works
       * @see https://scalablytyped.org/docs/usage
       * @param dependencies
       *   The list of TypeScript dependencies from NPM that you want to convert. This should be a Map value
       *   similar to the "dependencies" item from `package.json`:
       *   {{{
       *      Map(
       *        "react-router-dom" -> "5.1.2",
       *        "@types/react-router-dom" -> "5.1.2"
       *      )
       *   }}}
       *   Note that some packages contain first-party typescript type definitions, while for others
       *   like react-router-dom we need to get separate @types packages. These are originally from
       *   DefinitelyTyped.
       *   This parameter is required.
       * @param useNPM
       *   Whether to use NPM or Yarn. This parameter is optional and defaults to `true`.
       *   This helper checks for updated npm dependencies on each compile, and yarn responds much faster than npm.
       *   Yarn will need to be present on your system for this to work. You should also check in yarn.lock.
       * @param useScalaJsDom
       *   @see https://scalablytyped.org/docs/conversion-options#stusescalajsdom
       * @param minimizeAllTransitives
       *   When set to true, all transitive dependencies will be minimized. Otherwise, none will be. Exceptions
       *   to this rule can be made with the `exceptions` parameter.
       *   Default: true (minimize all transitive dependencies)
       * @param exceptions
       *   A list of exceptions to the "All" or "None" approach for `allTransitives` parameter.
       *   Default value is List.empty()
       * @see https://scalablytyped.org/docs/library-developer#compiling-all-that-generated-code
       * @param ignore
       *  A list of transitive dependencies to ignore (i.e. do not generate Scala.js facades for them)
       *  Default value is List.empty()
       * @see https://scalablytyped.org/docs/conversion-options#stignore
       * @param outputPackage
       *  The name of the scala package to which you want the Scala.js facades generated
       *  Default value: "org.ossum.sauce"
       * @param withDebugOutput
       *  Turn on verbose debug output. Default is false
       * @return Project
       */
      def scalablyTyped(
        useScalaJsDom: Boolean = false,
        minimizeAllTransitives: Boolean = true,
        exceptions: List[String] = List.empty[String],
        ignore: List[String] = List.empty[String],
        outputPackage: String = "org.ossum.sauce",
        withDebugOutput: Boolean = false
      )(project:Project): Project = {
        helpers.ScalablyTyped.withoutScalajsBundler(
          useScalaJsDom, minimizeAllTransitives, exceptions, ignore, outputPackage, withDebugOutput
        )(project)
      }
    }
  }

  override def projectSettings: Seq[Setting[_]] = Nil

}
