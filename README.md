# sbt-ossuminc

[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.17.2.svg)](https://www.scala-js.org)
[![scala-native](https://www.scala-native.org/assets/badges/scala-native-0.5.5.svg)](https://www.scala-native.org)

## Purpose

An sbt plugin that can be used for a wide range of projects. This plugin
is the only requirement for every project at Ossum Inc. and is maintained
by and for that company. However, it is likely quite useful for other
companies because of its modularity and ability to override the Ossum Inc.
defaults.

`sbt-ossuminc` is likely most helpful if you:

* Develop software in Scala (why else would you use `sbt` ? :) )
* Believe in using mono-repos containing many subprojects
* Need to support JVM, JS, and Native targets for your Scala code
* Work at the sbt command line and want lots of utilities there

`sbt-ossuminc` is designed to be opinionated and declarative, thus consequently simpler.
Because you _declare_ what you want and the defaults are usually correct. It is also 
less verbose than doing the equivalent by hand.

## Easy Setup

### project/plugins.sbt

In your `project/plugins.sbt` file, place this line:

```scala
addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "0.21.0")
```

### ~/sbt/1.0/github.sbt

You must also set up your credentials file as a global .sbt file. This file permits you
to read public repositories from GitHub Package Repository (such as those published by
Ossum Inc.) and managing private repositories in your organization(s). 

We recommend placing the credentials in your private home directory at `~/.sbt/1.0/github.sbt`.
It should have content like:

```text
credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "your-github-user-name-here",
  "your-github-token-here"
)
```
where:
* `your-github-user-name-here` is replaced with your Github user name
* `your-github-token-here` is replaced with the GitHub Personal Access token (classic) that you have generated with the  `repo` and `read:packages` privilege enabled.

### build.sbt

In your `build.sbt`, place this line near the top to enable everything `sbt-ossuminc` supports:

```scala
enablePlugins(OssumIncPlugin)
```

The above three things are required to activate the plugin for your build.
There's more you can do, as described below.

## Plugins included

Using that single plugin causes several other plugins to be adopted.
While you can use other `addSbtPlugin` declarations in `project/plugins.sbt`,
chances are you don't need to. The one line above also brings in all
the plugins listed in the sections below. These dependencies of 
`sbt-ossuminc` are regularly updated with help from
[Scala Steward](https://github.com/scala-steward-org/scala-steward) so all you have
to keep up to date is your version of `sbt-ossuminc` which Scala Steward
can also help you within your project. 


### Generic SBT Plugins

```scala
// Generic plugins from github.sbt project
addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.0.1")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.4")
addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.1")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")
addDependencyTreePlugin
```
* [sbt-dynver](https://github.com/sbt/sbt-dynver) - dynamic versioning based on git tags, commits, and date stamp
* [sbt-native-packager](https://github.com/sbt/sbt-native-packager) - packaging your compilation results into a package for various platforms
* [sbt-git]() - git commands from the sbt prompt
* [sbt-pgp]()- artifact signing for publishing to Sonatype
* [sbt-release]() - full control of the release process for your project
* [sbt-unidoc]() - unifying the documentation output from your programming language from several sub-projects
* `addDependencyTreePlugin` - adds a plugin so you can use the dependency commands at sbt prompt

### Plugins from other sources
```scala
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.12.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.10.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.11.1")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")
```
* [sbt-buildinfo]() - Your program can know all kinds of things about your build
* [sbt-header]() - Keep those file headers up to date with your project license
* [sbt-updates]() - TBD
* [sbt-sonatype]() - Publishing to Sonatype and Maven Central
* [sbt-github-packages]() - Publishing to Github Package Repository

### Scala Specific Plugins
```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.12.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.1.0")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.13")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.4")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.16.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.2")
```


## Standard Features Provided

Without any further definitions in your `build.sbt`, this plugin provides
various features that we like at Ossum Inc.:

* Git commands at the sbt prompt
* Dynamic versioning based on your git tag and updated on each sbt reload
* Automatic placement and update of your source file header comments
* Standardized Scala code formatting with scalameta from a single configuration file
* Automatic updates of dependencies with sbt-updates
* sbt-unidoc for collation of sub-project documentation into a single site
* sbt-native-packager for output packaging
* sbt-sonatype for publishing signed artifacts to Sonatype/Maven
* sbt-scoverage and sbt-coveralls for code coverage tracking

## Module Kinds

The `sbt-ossuminc` plugin automatically defines some top level objects you can
use to define your subprojects. The sub-sections below cover each of these
lightly. For more details see the scaladoc for the plugin.

### Root

Use this when you want to have a root project that aggregates all the other
sub-projects. When you've selected the root project (sbt command: `project root`)
then your commands get passed down to the sub-projects.

For example, this:

```scala 
lazy val riddl: Project = Root(
  ghRepoName = "my-project",
  ghOrgName = "my-organization",
  orgPackage = "com.my_org.my_proj",
  orgName = "My Organization",
  orgPage = url("https://my_org.com/"),
  maintainerEmail = "somebody@my_org.com",
  startYr = 2024)
  .configure(With.noPublishing, With.git, With.dynver)
  .aggregate(
    module0, // a sub-component of your project
    module1,
    module2
  )
```

defines a top-level Root project in the top level directory that aggregates the
three modules listed in the `.aggregate` call. The parameters to Root define the
basic identifiers about the project so you don't have to set them as sbt settings
elsewhere. You must define a Root in your `build.sbt` as it provides basic information
about your project that are used by other features of `sbt-ossuminc`.

### Module

So how do `module0`, `module1`, and `module2` get specified? With the `Module`
object of course!  Like this:

```scala
lazy val module0: Project = Module(dirName = "module0", modName = "proj-mod-0")
  .configure(With.typical, With.coverage(30))
  .configure(With.publishing)
  .settings(
    coverageExcludedPackages := "<empty>;$anon",
    description := "An example of a module sub-project",
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided"
    )
  )
  .dependsOn(module1)
```

The above defines a module named `proj-mod-0` in the directory named `module0`.
The module name will be used as the artifact name that the module compilation
produces. We've also asked for `With.typical` scala configuration and for code
coverage to be supported with at least 30% coverage, via `With.coverage(30)`.
This module is also configured to be published by using `With.publishing`.
All the `With` configuration options are described in a section below.

As you can see, you can still override the configured settings for your
subprojects. In the `.settings(...)`, it excludes empty packages from coverage, sets the module description
for publishing, and allows the scalajs annotations to be used, but just as stubs

### CrossModule

If you want to build your module for more than just the JVM, you can use the
CrossModule object in a pattern like this:

```scala
lazy val foo_cp: CrossProject = CrossModule(dirName = "foo", modName = "foo")(JVM, JS, Native)
  .dependsOn(other_cp)
  .configure(With.typical, With.publishing)
  .settings(
    scalacOptions ++= Seq("-explain", "--explain-types", "--explain-cyclic"),
    description := "The fooness of existence"
  )
  .jvmConfigure(With.coverage(30))
  .jvmSettings(
    coverageExcludedPackages := "<empty>;$anon",
  )
  .jsConfigure(With.js("RIDDL: passes", withCommonJSModule = true))
  .jsSettings(
    libraryDependencies += "com.foo" %%% "fooness" % "0.1.0"
  )
val passes = passes_cp.jvm
val passesJS = passes_cp.js
val passesNAT = passes_cp.native
```

There's a lot going on in this example. Most of it is based on `org.portable.scala`s plugins
`sbt-scalajs-crossproject`" and `sbt-scala-native-crossproject` which are used to implement
the `CrossModule` object. From top to bottom, we see that:

* a lazy val named `foo_cp` is defined as a `CrossProject` defined by those org.portable.scala plugins.
  It is named with the `_cp` suffix to distinguish it as the CrossProject (thing that can build any
  of the variants)
* The `CrossModule` object is invoked. The first set of arguments are just like for a module. The
  module lives in the "foo" directory and its published artifact names will start with "foo". The
  second argument list provides the kinds of targets to build. In this case all three: `JVM`, `JS`,
  and `Native`.
* The `.dependsOn`, `.configure` and `.settings` calls in the call chain are the typical ones for
  any sbt project but in this context they apply to all variants of what is to be built. Doing
  something specific to one, like a JS libraryDepenedency, will break your JVM build.
* The `.jvmConfigure` and `.jvmSettings` are analogous to the `.configure` and `.settings` call chain
  options, but they only apply to the JVM build.
* Similarly, the `.jsConfigure` and `.jsSettings` options only apply to the Javascript build.
* To support terseness at the command line, we define three values: `passes`, `passesJS` and
  `passesNAT` for each of the variants based on the recommendation by those crossproject plugins.
  This helps when you want to selectively run something like `passesJS/test` to run just the tests
  defined with the javascript variant

### Plugin

Use this to define an SBT plugin in a sub-project, like this:

```scala
lazy val plugin = Plugin(dirName = "sbt-plugin")
  .configure(With.build_info)
  .configure(With.scala2)
  .configure(With.publishing)
  .settings(
    description := "An sbt plugin to help the build world along",
    buildInfoObject := "SbtRiddlPluginBuildInfo",
    buildInfoPackage := "com.ossuminc.riddl.sbt",
    buildInfoUsePackageAsPath := true,
    scalaVersion := "2.12.19"
  )
```

In this example we define that the `sbt-plugin` is in the eponymous directory. We configure that
project with three things: `With.build_info`, `With.scala2`, and `With.publishing`. This automatically
incorporates the `scripted` plugin for testing sbt plugins.

### Program

Use this to define an executable Program with a `mainClass` like this:

```scala
lazy val program = Program(dirName = "my-program", programName = "myprog", mainClass = Option("com.myprog.Main"))
  .configure(With.typical, With.publishing)
  .dependsOn(
    module0,
    module1,
    module2,
  )
  .settings(
    description := "The main program",
    maintainer := "reid@ossuminc.com",
  )
```

By now you should be able to figure out the above settings. It will yield a program executable named
`myprog` from the contents of directory `my-program` that must define a class named `com.myprog.Main`
and will also include `module0`, `module1`, and `module2` in its classpath.

### DocSite

Use this top level definition to gather your api documentation into a web site. Here's an example from
the `ossuminc/riddl` repository:

```scala
lazy val docsite = DocSite(
  dirName = "doc",
  apiOutput = file("src") / "main" / "hugo" / "static" / "apidoc",
  baseURL = Some("https://riddl.tech/apidoc"),
  inclusions = Seq(utils, language, passes, diagrams, commands),
  logoPath = Some("doc/src/main/hugo/static/images/RIDDL-Logo-128x128.png")
)
  .settings(
    name := "riddl-doc",
    description := "Generation of the documentation web site",
    libraryDependencies ++= Dep.testing
  )
  .configure(With.noMiMa)
  .dependsOn(utils, language, passes, diagrams, commands)
```

## Configuration Helpers

Since the goal of `sbt-ossuminc` is to be declarative, we want to specify what we want to
include in the build. For this, we have the `With` options. These sbt configuration functions
transform a Project into another project with different settings. You can pass these names
directly into a `.configure` call chain invocation per usual `sbt` usage.

Here are descriptions of all the parameterless configuration functions you can use:

* `With.aliases` - add a set of useful command line aliases to the sbt command line
* `With.build_info` - enable the `sbt-buildinfo` plugin on the project to get an info object
  that provides info about your project
* `With.dynver` - enable the `sbt-dynver` plugin on the project for dynamic versioning based
  on git tags
* `With.git` - enable issuing any `git` command from the sbt prompt so you don't need to use
  another terminal window.
* `With.header` - enable the `sbt-header` plugin for replacing code header comments with copyright
* `With.java` - enable javac compiler in your project to compile Java as well as Scala
* `With.noMiMa` - turn Migration Manager off for this project (prevents build-stopping errors)
* `With.publishing` - configure the project to publish signed artifacts to Sonatype/Maven
* `With.release` - enable the `sbt-release` plugin
* `With.resolvers` - enable a group of resolvers for resolving dependencies
* `With.scala2` - configure the project to do the latest Scala 2 compilation
* `With.scala3` - configure the Project to do the most recent LTS Scala 3 compilation
* `With.scalaTest` - add ScalaTest dependencies to the probject
* `With.scalaft` - add standardized ScalaFmt formatting help
* `With.scoverage` - add `sbt-scoverage` and `sbt-coveralls` support to the project
* `With.basic` - shorthand for adding these: aliases, dynver, git, header, resolvers
* `With.typical` - shorthand for With.basic and scala3, scalaTest and publishing
* `With.everything` - shorthand for With.typical and java, misc, build_info, release
* `With.noPublishing` - turns publishing off making the project unpublishable, handy for Root project
* `With.plugin` - makes the project produce an sbt autoplugin

Here are the descriptions of the configuration functions that take parameters:

* `With.akka(release: String = """)`
    - add the Akka dependencies to the project that correspond to `release`
    - release should be `24.05` or `24.10` but it defaults to `24.10`

* `With.coverage(percent: Double= 50.0d)`
    - enable code coverage plugins with the following options:

* `With.js` - enable Scala.js Javascript compilation with the following options:
    * header: String = "no header"
    * hasMain: Boolean = false,
    * forProd: Boolean = true,
    * withCommonJSModule: Boolean = false

* `With.laminar` - for Scala.js modules, include laminar and DOM support with the following options:
    * version: String = "17.1.0"
    * domVersion: String = "2.8.0"
    * waypointVersion: Option[String] = None
    * laminextVersion: Option[String] = None
    * laminextModules: Seq[String] = Seq.empty

* `With.MiMa` - turns on Migration manager, and you must specify at least the first option:
    * previousVersion: String // the previous version of the artifact/project to check against
    * excludedClasses: Seq[String] = Seq.empty // classes to exclude from compatibility checks
    * reportSignatureIssues: Boolean = false // full signature checks, including generic type parameters

* `With.native` - enable native code generation with `scala-native` with the following options:
    - mode: String = "fast",
    - buildTarget: String = "static",
    - gc: String = "boehm",
    - lto: String = "none",
    - debugLog: Boolean = false,
    - verbose: Boolean = false,
    - targetTriple: Option[String] = None,
    - linkOptions: Seq[String] = Seq.empty

* `With.packagingUniversal(maintainerEmail, pkgName, pkgSummary, pgkDescription)`
    - enable universal packaging via sbt-native-packager, with the following options:
        - maintainerEmail: String,
        - pkgName: String,
        - pkgSummary: String,
        - pkgDescription: String

* `With.packagingDocker(maintainerEmail, pkgName, pkgSummary, pgkDescription)`
    - enable packaging as a Docker image via sbt-native-packager, with the following options:
        - maintainerEmail: String,
        - pkgName: String,
        - pkgSummary: String,
        - pkgDescription: String

* `With.riddl(version: String, nonJVM: Boolean = true)`
    - include a specific version of RIDDL libraries with the following options:
        * `version` - the version of RIDDL to include
        * `nonJVM` - the kind of module to include with %% (JVM) or %%% (JS, Native)

* `With.unidoc(apiOutput, baseURL, inclusions, exclusions, logoPath, externalMappings)`
    - enable the `sbt-unidoc` plugin with the following options:
        * apiOutput: File = file("target/unidoc")
            * where in the local build to generate the output
        * baseURL: Option[String] = None
        * inclusions: Seq[ProjectReference] = Seq.empty
        * exclusions: Seq[ProjectReference] = Seq.empty
        * logoPath: Option[String] = None
        * externalMappings: Seq[Seq[String]] = Seq.empty

  


