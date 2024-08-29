# sbt-ossuminc
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.16.0.svg)](https://www.scala-js.org)

## Purpose 
An sbt plugin that can be used for a wide range of projects. This plugin
is the only requirement for every project at Ossum Inc. and is maintained
by and for that company. However, it is likely quite useful for other
companies because of its modularity and ability to override the Ossum Inc. 
defaults. 

`sbt-ossuminc` is likely most useful if you:
* Believe in using mono-repos containing many subprojects
* Need to support JVM, JS, and Native targets for your Scala code
* Work at the sbt command line and want lots of utilities there

`sbt-ossuminc` is designed to be opinionated and declarative, and consequently simpler. 
Because you just _declare_ what you want and the defaults are usually correct, it is less verbose
as well.

## Easy Setup
In your `project/plugins.sbt` file, place this line:

```scala
addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "0.13.3")
```

In your `build.sbt` place this line near the top:

```scala
enablePlugins(OssumIncPlugin)
```

These two things are required to activate the plugin for your build. 
There's more you can do, described below. 

### Plugins included
Using that single plugin causes several other plugins to be adopted. 
While you can use other `addSbtPlugin` declarations in `project/plugins.sbt`,
chances are you don't need to. The one line above also brings in these
common plugins:

```scala
// Generic plugins from github.sbt project
addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.0.1")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.4")
addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.1")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")
addDependencyTreePlugin

// Helpers from other sources
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.12.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.10.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.11.1")

// Scala specific from various places
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.12.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.1.0")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.13")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.4")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.16.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.2")
```
These dependencies of `sbt-ossuminc` are regularly updated with help from 
[Scala Steward](https://github.com/scala-steward-org/scala-steward) so all you have
to keep up to date is your version of `sbt-ossuminc`

## Standard Features Provided
Without any further definitions in your `build.sbt`, this plugin provides 
various features that we like at Ossum Inc.:

* Git commands at the sbt prompt
* Dynamic versioning based on your git tag and updated on each sbt reload
* Automatic placement and update of your source file header comments
* Standardized code formatting with scalameta from a single configuration file
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
elsewhere. 

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
lazy val foo_cp: CrossProject = CrossModule(dirName= "foo", modName= "foo")(JVM,JS,Native)
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
the `CrossModule` object.  From top to bottom, we see that:
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
lazy val plugin = Plugin(dirName="sbt-plugin")
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
lazy val program = Program(dirName="my-program", programName="myprog", mainClass=Option("com.myprog.Main"))
  .configure(With.typical,With.publishing)
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


## With Options
Since the goal of `sbt-ossuminc` is to be declarative, we want to specify just what we want to 
include in the build. For this we have the `With` options. These are sbt configuration functions
that transform a Project into another project with different settings. You can pass these names
directly into a `.configure` call chain invocation per usual `sbt` usage. 

Here are descriptions of all the parameterless configuration functions you can use:
* `With.akka` - add the latests set of Akka dependencies to the project
* `With.aliases` - add a set of useful command line aliases to the sbt command line
* `With.build_info` - enable the `sbt-buildinfo` plugin on the project to get an info object 
  that provides info about your project
* `With.dynver` - enable the `sbt-dynver` plugin on the project for dynamic versioning based 
  on git tags
* `With.git` - enable issuing any `git` command from the sbt prompt so you don't need to use
  another terminal window.
* `With.header` - enable the `sbt-header` plugin for replacing code header comments with copyright 
* `With.java` -
* `With.javascript` - 
* `With.misc` -
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

* `With.unidoc` - enable the `sbt-unidoc` plugin with the following options:
  * apiOutput: File = file("target/unidoc")
  * baseURL: Option[String] = None
  * inclusions: Seq[ProjectReference] = Seq.empty
  * exclusions: Seq[ProjectReference] = Seq.empty
  * logoPath: Option[String] = None
  * externalMappings: Seq[Seq[String]] = Seq.empty

* `With.coverage` - enable code coverage plugins with the following options:
  * percent: Double = 50.0d

* `With.js` - enable Scala.js Javascript compilation with the following options: 
  * header: String = "no header"
  * hasMain: Boolean = false,
  *  forProd: Boolean = true,
  *  withCommonJSModule: Boolean = false

* `With.native` - enable native code generation with `scala-native` with the following options: 
  buildTarget: String = "static"
  targetTriple: String = "arm64-apple-macosx11.0.0"
  gc: String = "commix"
  debug: Boolean = true
  noLTO: Boolean = false
  debugLog: Boolean = false
  verbose: Boolean = false
  ld64Path: String = "/opt/homebrew/opt/llvm/bin/ld64.lld"

* `With.laminar` - for Scala.js modules, include laminar and DOM support with the following options:
  * version: String = "17.1.0"
  * domVersion: String = "2.8.0"

* `With.riddl` - for JVM or JS modules, include a specific version of RIDDL libraries with the
  following options:
  * forJS: Boolean
  * version: String
  


