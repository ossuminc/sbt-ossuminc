package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*

import java.io.File
import java.nio.charset.Charset

/** Unit Tests For JFlex */
object JFlex extends AutoPluginHelper {

  object Keys {

    var jflexJlexcompatibility: SettingKey[Boolean] = settingKey[Boolean]("strict JLex compatibility")
    var jflexNoMinimize: SettingKey[Boolean] = settingKey[Boolean]("don't run minimization algorithm if this is true")
    var jflexNoBackup: SettingKey[Boolean] = settingKey[Boolean]("don't write backup files if this is true")
    var jflexVerbose: SettingKey[Boolean] = settingKey[Boolean]("if false, only error/warning output will be generated")
    var jflexTime: SettingKey[Boolean] =
      settingKey[Boolean]("if true, jflex will print time statistics about the generation process")
    var jflexDot: SettingKey[Boolean] =
      settingKey[Boolean]("If true, jflex will write graphviz .dot files for generated automata")
    var jflexEncoding: SettingKey[String] = settingKey[String]("Unicode encoding to expect on input")
    val jflexFileSuffix = SettingKey[String](
      "the suffix of jflex files to parse, typically '.flex'"
    )
    val generate = TaskKey[Seq[File]]("generate")
  }

  private case class JFlexOptions(
    compatibility: Boolean,
    no_minimize: Boolean,
    no_backup: Boolean,
    verbose: Boolean,
    time: Boolean,
    dot: Boolean,
    encoding: Charset
  )

  val Jflex = config("jflex")
  final case class PluginConfiguration(fileSuffix: String = ".flex")

  /** The configuration function to call for this plugin helper
    *
    * @param project
    *   The project to which the configuration should be applied
    * @return
    *   The same project passed as an argument, post configuration
    */
  override def configure(project: Project): Project = {
    import Keys.*
    project.settings(
      jflexJlexcompatibility := false,
      jflexNoMinimize := false,
      jflexNoBackup := false,
      jflexVerbose := false,
      jflexTime := false,
      jflexDot := false,
      jflexEncoding := "utf-8",
      jflexFileSuffix := ".flex",
      Jflex / sourceDirectory := (Compile / sourceDirectory).value / "flex",
      Jflex / target := (Compile / sourceDirectory).value / "flex-java",
      Compile / managedSources += (Jflex / target).value / "*.flex",
      Compile / sourceGenerators += Keys.generate.taskValue,
      ivyConfigurations += Jflex,
      cleanFiles += (Jflex / target).value,
      Jflex / target := (Compile / sourceManaged).value,
      Compile / unmanagedSourceDirectories += (Jflex / sourceDirectory).value,
      Keys.generate := {
        val out = streams.value
        val options = JFlexOptions(
          jflexJlexcompatibility.value,
          jflexNoMinimize.value,
          jflexNoBackup.value,
          jflexVerbose.value,
          jflexTime.value,
          jflexDot.value,
          Charset.forName(jflexEncoding.value, Charset.defaultCharset())
        )
        // Generate a function that translates input flex files to
        val cachedGeneration: Set[File] => Set[File] = FileFunction.cached(
          out.cacheDirectory / "flex",
          inStyle = FilesInfo.lastModified,
          outStyle = FilesInfo.exists
        ) { (in: Set[File]) =>
          generateWithJFlex(in, (Jflex / target).value, options, out.log)
        }
        val sourceDir: File = (Jflex / sourceDirectory).value
        val inputs: Set[File] = (sourceDir ** s"*.${Keys.jflexFileSuffix.value}").get().toSet
        cachedGeneration(inputs).toSeq
      }
    )
  }

  private def generateWithJFlex(sources: Set[File], target: File, options: JFlexOptions, log: Logger) = {
    import jflex.generator.LexGenerator
    import jflex.core.OptionUtils
    import jflex.option.Options

    // configure jflex options directly
    log.info(s"JFlex: Using JFlex version ${jflex.base.Build.VERSION} to generate source files.")
    Options.dump = false
    Options.legacy_dot = false
    Options.progress = false
    Options.dot = options.dot
    Options.verbose = options.verbose
    Options.time = options.time
    Options.encoding = options.encoding
    Options.jlex = options.compatibility
    Options.no_backup = options.no_backup
    Options.no_minimize = options.no_minimize
    OptionUtils.setDir(target)

    // Indicate what we're about to do
    log.info("JFlex: Generating source files for %d source grammars.".format(sources.size))

    // prepare the output directory for the source files ensuring intermediate directories are created
    target.mkdirs()

    // Run each grammar through the LexGenerator tool to produce a Java source input
    sources foreach { s =>
      log.info("JFlex: Grammar file '%s' detected.".format(s.getPath))
      new LexGenerator(s).generate()
    }

    // Return the resulting java files.
    (target ** "*.java").get.toSet
  }

}
