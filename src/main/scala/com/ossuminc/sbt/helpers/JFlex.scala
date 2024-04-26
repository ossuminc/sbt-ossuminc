package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.{sourceDirectory, *}

/** Unit Tests For JFlex */
object JFlex extends AutoPluginHelper {

  object Keys {
    val jflexOptions: SettingKey[Seq[String]] = settingKey[Seq[String]](
      "options to pass to the jflex program other than source file"
    )
    val jflexFileSuffix = SettingKey[String](
      "the suffix of jflex files to parse, typically '.flex'"
    )
    val generate = TaskKey[Seq[File]]("generate")
  }

  val Jflex = config("jflex")
  final case class PluginConfiguration(fileSuffix: String = ".flex")

  /** The configuration function to call for this plugin helper
   *
   * @param project
   * The project to which the configuration should be applied
   * @return
   * The same project passed as an argument, post configuration
   */
  override def configure(project: Project): Project = {
    project.settings(
      Keys.jflexOptions := Seq(
        "--encoding", "utf-8",
        "-q", "--time"
      ),
      sourceDirectories += baseDirectory.value / "src" / "main" / "flex",
      managedClasspath := Classpaths.managedJars(Jflex, (Jflex / classpathTypes).value, update.value),
      Keys.generate := {
        val out = streams.value
        val options = Keys.jflexOptions.value
        val cacheDir = FileFunction.cached(
          out.cacheDirectory / "flex", inStyle = FilesInfo.lastModified, outStyle = FilesInfo.exists
        )
        val cachedCompile = cacheDir { (in: Set[File]) =>
          generateWithJFlex(in, (Jflex / target ).value,
            (Jflex / toolConfiguration).value, options, out.log)
        }
        cachedCompile(((Jflex/sourceDirectory).value ** ("*" + options.fileSuffix)).get.toSet).toSeq
      }

    )
  }

  val jflexDependency = SettingKey[ModuleID]("jflex-dependency")
  val toolConfiguration = SettingKey[JFlexToolConfiguration]("jflex-tool-configuration")

  /**
   * use this if you don't want jflex to run automatically (because, e.g., you're checking it in)
   * you'll want to set [[target]] in [[jflex]] using [[unmanagedJflexSettings]] or your own variant
   */
  lazy val commonJflexSettings: Seq[Def.Setting[_]] = inConfig(Jflex)(Seq(
    toolConfiguration := JFlexToolConfiguration(),
    pluginConfiguration := PluginConfiguration(),
    jflexDependency := "de.jflex" % "jflex" % "1.6.1",

    sourceDirectory := (Compile / sourceDirectory).value / "flex",



  )) ++ Seq(
    libraryDependencies += (jflexDependency in Jflex).value,
    ivyConfigurations += Jflex
  )

  lazy val unmanagedJflexSettings = commonJflexSettings ++ inConfig(Jflex)(Seq(
    Jflex / target := (javaSource in Compile).value,
    managedSources := (Jflex / Keys.generate).value
  ))

  lazy val jflexSettings: Seq[Def.Setting[_]] = commonJflexSettings ++
    inConfig(Jflex)(
      Jflex / target := (Compile / sourceManaged).value
    ) ++ Seq(
    compile / unmanagedSourceDirectories += (Jflex / sourceDirectory).value,
    Compile / sourceGenerators += (Jflex / Keys.generate).taskValue,
    cleanFiles += (target in Jflex).value
  )

  private def generateWithJFlex(sources: Set[File], target: File, tool: JFlexToolConfiguration,
    options: Seq[String], log: Logger) = {
    import jflex.generator.LexGenerator

    // prepare target
    target.mkdirs()

    // configure jflex tool
    log.info(s"JFlex: Using JFlex version ${jflex.base.Build.VERSION} to generate source files.")
    Options.dot = tool.dot
    Options.verbose = tool.verbose
    Options.dump = tool.dump
    OptionUtils.setDir(target.getPath)

    // process grammars
    val grammars = sources
    log.info("JFlex: Generating source files for %d grammars.".format(grammars.size))

    // add each grammar file into the jflex tool's list of grammars to process
    grammars foreach { g =>
      log.info("JFlex: Grammar file '%s' detected.".format(g.getPath))
      new LexGenerator(g).generate()
    }

    (target ** ("*.java")).get.toSet
  }

}
