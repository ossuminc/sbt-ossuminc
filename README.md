# sbt-ossuminc

[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.20.1.svg)](https://www.scala-js.org)
[![scala-native](https://www.scala-native.org/assets/badges/scala-native-0.5.9.svg)](https://www.scala-native.org)

## Purpose

An sbt plugin that can be used for a wide range of projects. This plugin
is the only requirement for every project at Ossum Inc. and is maintained
by and for that company. However, it is likely quite useful for other
companies because of its modularity and ability to override the Ossum Inc.
defaults.

`sbt-ossuminc` is likely most helpful if you:

* Develop software in Scala (why else would you use `sbt`? :) )
* Believe in using mono-repos containing many subprojects
* Need to support JVM, JS, and Native targets for your Scala code
* Work at the sbt command line and want lots of utilities there

## Design Philosophy

`sbt-ossuminc` embraces a **functional, minimalist, Don't-Repeat-Yourself (DRY)** approach to build configuration:

### Declarative Over Imperative
Instead of writing imperative sbt settings, you **declare what you want** using composable configuration helpers. The plugin handles the details.

```scala
// ❌ Imperative (verbose, repetitive)
lazy val myModule = project
  .settings(scalaVersion := "3.3.7")
  .settings(scalacOptions ++= Seq("-deprecation", "-feature"))
  .settings(libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test)
  .enablePlugins(GitPlugin, DynVerPlugin)
  // ... 20 more lines of boilerplate

// ✅ Declarative (concise, clear intent)
lazy val myModule = Module("my-module")
  .configure(With.typical)
```

### Composable Helpers
Configuration helpers are **pure functions** (`Project => Project`) that compose naturally:

```scala
Module("my-lib")
  .configure(With.typical)         // Scala 3 + testing + versioning + git
  .configure(With.coverage(80))    // Add code coverage with 80% threshold
  .configure(With.GithubPublishing) // Publish to GitHub Packages
  .dependsOn(otherModule)
```

### Sensible Defaults
Defaults are chosen for **modern Scala development** but can always be overridden:

- **Scala 3.3.7 LTS** by default (override with `With.Scala3(version = Some("3.4.0"))`)
- **Cross-platform ready** (JVM, JS, Native with one declaration)
- **Dynamic versioning** from git tags (no manual version management)
- **Automatic header management** (keep license headers current)

### DRY Principle
Define once, use everywhere. No copy-paste configuration across subprojects:

```scala
// Define your standard configuration once
val standardModule = (p: Project) => p
  .configure(With.typical, With.coverage(70), With.GithubPublishing)

// Apply it to multiple modules
lazy val moduleA = Module("module-a").configure(standardModule)
lazy val moduleB = Module("module-b").configure(standardModule)
lazy val moduleC = Module("module-c").configure(standardModule)
```

## Easy Setup

### project/plugins.sbt

In your `project/plugins.sbt` file, add the GitHub Packages resolver and the plugin:

```scala
// GitHub Packages resolver for sbt-ossuminc
resolvers += "GitHub Packages" at "https://maven.pkg.github.com/ossuminc/sbt-ossuminc"

addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "1.1.0")
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
* [sbt-git](https://github.com/sbt/sbt-git) - git commands from the sbt prompt
* [sbt-pgp](https://github.com/sbt/sbt-pgp) - artifact signing for publishing to Sonatype
* [sbt-release](https://github.com/sbt/sbt-release) - full control of the release process for your project
* [sbt-unidoc](https://github.com/sbt/sbt-unidoc) - unifying the documentation output from your programming language from several sub-projects
* `addDependencyTreePlugin` - adds a plugin so you can use the dependency commands at sbt prompt

### Plugins from other sources
```scala
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.10.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.11.1")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox" % "0.9.2")
```
* [sbt-buildinfo](https://github.com/sbt/sbt-buildinfo) - Your program can know all kinds of things about your build
* [sbt-header](https://github.com/sbt/sbt-header) - Keep those file headers up to date with your project license
* [sbt-updates](https://github.com/rtimush/sbt-updates) - Check for dependency updates
* [sbt-sonatype](https://github.com/xerial/sbt-sonatype) - Publishing to Sonatype and Maven Central
* [sbt-github-packages](https://github.com/djspiewak/sbt-github-packages) - Publishing to Github Package Repository
* [sbt-paradox](https://github.com/lightbend/paradox) - Markdown documentation generator

### Scala Specific Plugins
```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.5")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.4.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.15")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.9")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.20.1")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.2")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.4")
addSbtPlugin("ch.epfl.scala" % "sbt-tasty-mima" % "1.2.0")
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta44")
addSbtPlugin("org.jetbrains.scala" % "sbt-idea-plugin" % "5.0.4")
```
* [sbt-scalafix](https://github.com/scalacenter/sbt-scalafix) - Code refactoring and linting
* [sbt-scalafmt](https://github.com/scalameta/sbt-scalafmt) - Code formatting
* [sbt-scoverage](https://github.com/scoverage/sbt-scoverage) - Code coverage measurement
* [sbt-coveralls](https://github.com/scoverage/sbt-coveralls) - Upload coverage to Coveralls.io
* [sbt-scala-native](https://github.com/scala-native/scala-native) - Compile Scala to native code
* [sbt-scalajs](https://github.com/scala-js/scala-js) - Compile Scala to JavaScript
* [sbt-scalajs-crossproject](https://github.com/portable-scala/sbt-crossproject) - Cross-platform builds (JVM/JS)
* [sbt-scala-native-crossproject](https://github.com/portable-scala/sbt-crossproject) - Cross-platform builds (JVM/Native)
* [sbt-platform-deps](https://github.com/portable-scala/sbt-platform-deps) - Platform-specific dependencies
* [sbt-mima-plugin](https://github.com/lightbend-labs/mima) - Binary compatibility checking
* [sbt-tasty-mima](https://github.com/scalacenter/tasty-mima) - TASTy compatibility checking
* [sbt-converter](https://github.com/ScalablyTyped/Converter) - Generate Scala.js facades from TypeScript
* [sbt-idea-plugin](https://github.com/JetBrains/sbt-idea-plugin) - IntelliJ IDEA plugin development


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
  .jsConfigure(With.ScalaJS("RIDDL: passes", withCommonJSModule = true))
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
include in the build. For this, we have the `With.*` configuration helpers. These are
pure functions (`Project => Project`) that transform projects by adding settings and plugins.
You can pass these directly into a `.configure()` call chain.

### Basic Configuration Helpers

These helpers take no parameters (or use all defaults):

* **`With.aliases`** - Add useful command line aliases to the sbt shell
* **`With.build_info`** - Enable `sbt-buildinfo` plugin (default configuration)
* **`With.dynver`** - Enable `sbt-dynver` for git-based dynamic versioning
* **`With.git`** - Enable `sbt-git` to issue git commands from sbt prompt
* **`With.header`** - Enable `sbt-header` for automatic license header management
* **`With.java`** - Enable javac compiler for Java/Scala projects
* **`With.scalajs`** - Enable Scala.js compilation (default configuration)
* **`With.noMiMa`** - Disable binary compatibility checking
* **`With.noPublishing`** - Disable artifact publishing (useful for root aggregator projects)
* **`With.release`** - Enable `sbt-release` plugin
* **`With.resolvers`** - Add standard resolvers (Maven Local, JCenter, Typesafe)
* **`With.scala2`** - Configure for Scala 2.13 (latest)
* **`With.scala3`** - Configure for Scala 3.3.7 LTS (default)
* **`With.scoverage`** - Enable code coverage with sbt-scoverage and sbt-coveralls

### Publishing Helpers

* **`With.GithubPublishing`** - Configure publishing to GitHub Packages
* **`With.SonatypePublishing`** - Configure publishing to Sonatype/Maven Central

> **Note**: Do not combine GithubPublishing and SonatypePublishing in the same project.

### Composite Helpers

Shortcuts that combine multiple helpers:

* **`With.basic`** - Combines: `aliases`, `dynver`, `git`, `header`, `resolvers`
* **`With.typical`** - Combines: `basic`, `scala3`, `Scalatest()`
* **`With.everything`** - Combines: `typical`, `java`, `release`

### Parameterized Configuration Helpers

These helpers accept parameters for customization:

#### **`With.Akka.forRelease(release: String)`**
Add Akka dependencies to the project. Akka requires a commercial license and repository token since 2024.
- **`release`**: Akka version (`"24.10"` or `"25.10"` (latest))

```scala
Module("my-actor-system")
  .configure(With.Akka.forRelease("25.10"))
```

> **Note**: Akka repository access requires a token. Configure per Akka's instructions at https://akka.io/key

#### **`With.AsciiDoc(...)`**
Configure AsciiDoc document generation for static websites and PDFs.
- **`sourceDir`**: Directory containing AsciiDoc source files (default: `"src/asciidoc"`)
- **`enablePdf`**: Enable PDF generation (default: `true`)
- **`enableDiagrams`**: Enable diagram support for PlantUML, Graphviz, etc. (default: `false`)
- **`attributes`**: Custom AsciiDoc attributes for document processing

Features:
- HTML5 website generation with customizable attributes
- PDF generation with asciidoctorj-pdf
- Diagram support via asciidoctorj-diagram (PlantUML, Graphviz, etc.)
- Customizable source directories and output locations

```scala
DocSite("docs", "project-docs")
  .configure(With.AsciiDoc(
    sourceDir = "src/docs/asciidoc",
    enablePdf = true,
    enableDiagrams = true,
    attributes = Map("toc" -> "left", "icons" -> "font")
  ))
```

> **Note**: For full HTML generation via sbt-site, add to `project/plugins.sbt`:
> ```scala
> addSbtPlugin("com.github.sbt" % "sbt-site-asciidoctor" % "1.7.0")
> ```
> Then enable in `build.sbt`: `enablePlugins(AsciidoctorPlugin)`

#### **`With.BuildInfo(...)`**
Customize BuildInfo generation.
- **`buildInfoObject`**: Name of generated object
- **`buildInfoPackage`**: Package for generated code
- **`buildInfoUsePackageAsPath`**: Use package as directory structure

```scala
Module("my-app")
  .configure(With.BuildInfo(
    buildInfoObject = "AppBuildInfo",
    buildInfoPackage = "com.myapp.build"
  ))
```

#### **`With.coverage(percent: Double = 50.0)`**
Enable code coverage with minimum threshold.

```scala
Module("my-lib")
  .configure(With.coverage(80.0))  // Require 80% coverage
```

#### **`With.IdeaPlugin(...)`**
Configure IntelliJ IDEA plugin development.
- **`name`**: Plugin name
- **`description`**: Plugin description
- **`build`**: IntelliJ build version (e.g., `"243.x"`)
- **`platform`**: `"Community"` or `"Ultimate"`

```scala
Plugin("my-idea-plugin")
  .configure(With.IdeaPlugin(
    name = "My Cool Plugin",
    build = "243.x",
    platform = "Community"
  ))
```

#### **`With.ScalaJS(...)`**
Configure Scala.js compilation.
- **`header`**: JS file header comment
- **`hasMain`**: Enable main module initializer
- **`forProd`**: Enable optimizer (production mode)
- **`withCommonJSModule`**: Use CommonJS modules instead of ES modules

```scala
CrossModule("my-ui", "ui")(JVM, JS)
  .jsConfigure(With.ScalaJS(
    header = "My App UI v1.0",
    hasMain = true,
    forProd = true
  ))
```

> **Note**: `With.Javascript` is deprecated and will be removed in 2.0. Use `With.ScalaJS` instead.

#### **`With.Laminar(...)`**
Add Laminar reactive UI dependencies (Scala.js).
- **`version`**: Laminar version
- **`domVersion`**: Scala.js DOM version
- **`waypointVersion`**: Waypoint router version (optional)
- **`laminextVersion`**: Laminext utilities version (optional)
- **`laminextModules`**: Specific Laminext modules to include

```scala
CrossModule("frontend", "app-frontend")(JS)
  .jsConfigure(With.Laminar(
    version = "17.1.0",
    domVersion = "2.8.0"
  ))
```

#### **`With.MiMa(...)`**
Enable binary compatibility checking.
- **`previousVersion`**: Version to check compatibility against (required)
- **`excludedClasses`**: Classes to exclude from checks
- **`reportSignatureIssues`**: Include generic type parameter checks

```scala
Module("my-stable-api")
  .configure(With.MiMa(
    previousVersion = "1.0.0",
    excludedClasses = Seq("com.myapp.internal.*")
  ))
```

#### **`With.Native(...)`**
Configure Scala Native compilation.
- **`mode`**: Compilation mode (`"debug"`, `"fast"`, `"full"`, `"size"`, `"release"`)
- **`buildTarget`**: Build type (`"application"`, `"dynamic"`, `"static"`)
- **`gc`**: Garbage collector to use
- **`lto`**: Link-time optimization (`"none"`, `"thin"`, `"full"`)
- **`debugLog`**: Enable debug logging
- **`verbose`**: Verbose compilation output
- **`targetTriple`**: Target platform triple (optional)
- **`linkOptions`**: Additional linker options

```scala
CrossModule("cli-tool", "tool")(JVM, Native)
  .nativeConfigure(With.Native(
    mode = "release",
    buildTarget = "application"
  ))
```

#### **`With.Packaging.universal(...)`**
Create universal (zip/tgz) packages.
- **`maintainerEmail`**: Package maintainer email
- **`pkgName`**: Package name
- **`pkgSummary`**: One-line summary
- **`pkgDescription`**: Full description

```scala
Program("my-app", "app", Some("com.myapp.Main"))
  .configure(With.Packaging.universal(
    maintainerEmail = "dev@example.com",
    pkgName = "my-app",
    pkgSummary = "My Application",
    pkgDescription = "A useful application"
  ))
```

#### **`With.Packaging.docker(...)`**
Create Docker images.
- **`maintainerEmail`**: Package maintainer email
- **`pkgName`**: Docker image name
- **`pkgSummary`**: Image summary
- **`pkgDescription`**: Image description

```scala
Program("my-service", "service", Some("com.myapp.Service"))
  .configure(With.Packaging.docker(
    maintainerEmail = "dev@example.com",
    pkgName = "my-service"
  ))
```

#### **`With.Packaging.graalVM(...)`**
Create GraalVM native images.
- **`pkgName`**: Executable name
- **`pkgSummary`**: Summary
- **`native_image_path`**: Path to native-image executable

#### **`With.Riddl(...)`**
Add RIDDL library dependencies.
- **`version`**: RIDDL version to use
- **`nonJVM`**: Use `%%%` (true) or `%%` (false) for dependency resolution

```scala
Module("my-riddl-app")
  .configure(With.Riddl(version = "0.50.0"))
```

#### **`With.Scala3(...)`**
Configure Scala 3 with custom version or compiler options.
- **`version`**: Scala version (defaults to `"3.3.7"`)
- **`scala3Options`**: Additional compiler options

```scala
Module("my-experimental")
  .configure(With.Scala3(
    version = Some("3.4.0"),
    scala3Options = Seq("-experimental")
  ))
```

#### **`With.ScalablyTyped(...)`**
Generate Scala.js facades from TypeScript definitions.

#### **`With.Scalatest(...)`**
Add ScalaTest dependencies with custom version.
- **`version`**: ScalaTest version

```scala
Module("my-tests")
  .configure(With.Scalatest(version = "3.2.19"))
```

#### **`With.Unidoc(...)`**
Generate unified API documentation.
- **`apiOutput`**: Output directory
- **`baseURL`**: Base URL for documentation
- **`inclusions`**: Projects to include
- **`exclusions`**: Projects to exclude
- **`logoPath`**: Path to logo image
- **`externalMappings`**: External API mappings

```scala
DocSite(
  dirName = "docs",
  apiOutput = file("docs/api"),
  baseURL = Some("https://myproject.org/api"),
  inclusions = Seq(moduleA, moduleB, moduleC)
)
```

  


