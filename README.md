# sbt-ossuminc

[![sbt](https://img.shields.io/badge/sbt-2.0.0-blue.svg)](https://www.scala-sbt.org)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.22.0.svg)](https://www.scala-js.org)
[![scala-native](https://www.scala-native.org/assets/badges/scala-native-0.5.12.svg)](https://www.scala-native.org)

> **sbt 2.x:** `sbt-ossuminc` 2.0.0+ requires **sbt 2.0.2+** and **Scala 3**.
> For sbt 1.x builds, use the 1.x line (last release **v1.4.0**).

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
  .settings(scalaVersion := "3.8.4")
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

- **Scala 3.8.4** (Scala Next) by default (override with `With.Scala3(version = Some("3.3.8"))`)
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

**Requirements:** sbt **2.0.0**+, JDK **17+** (Temurin 25 recommended), Scala **3**.

### project/build.properties

Pin sbt 2.0.2 (or newer) in your `project/build.properties`:

```properties
sbt.version=2.0.2
```

### project/plugins.sbt

In your `project/plugins.sbt` file, add the GitHub Packages resolver and the plugin:

```scala
// GitHub Packages resolver for sbt-ossuminc
resolvers += "GitHub Packages" at "https://maven.pkg.github.com/ossuminc/sbt-ossuminc"

addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "2.0.0")
```

### ~/.sbt/2/github.sbt

You must also set up your credentials file as a global .sbt file. This file permits you
to read public repositories from GitHub Package Repository (such as those published by
Ossum Inc.) and managing private repositories in your organization(s).

We recommend placing the credentials in your private home directory at `~/.sbt/2/github.sbt`
(sbt 2 reads global configuration from `~/.sbt/2/`, not `~/.sbt/1.0/`).
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
// Generic plugins from the github.sbt project
addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.1.1")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.7")
addSbtPlugin("com.github.sbt" % "sbt-git" % "2.1.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.5.0")
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.6.1")
// dependency-tree is built into sbt 2 — no addDependencyTreePlugin needed
```
* [sbt-dynver](https://github.com/sbt/sbt-dynver) - dynamic versioning based on git tags, commits, and date stamp
* [sbt-native-packager](https://github.com/sbt/sbt-native-packager) - packaging your compilation results into a package for various platforms
* [sbt-git](https://github.com/sbt/sbt-git) - git commands from the sbt prompt
* [sbt-pgp](https://github.com/sbt/sbt-pgp) - artifact signing (`publishSigned`) for Maven Central via the Central Portal
* [sbt-release](https://github.com/sbt/sbt-release) - full control of the release process for your project
* [sbt-unidoc](https://github.com/sbt/sbt-unidoc) - unifying the documentation output from your programming language from several sub-projects
* dependency-tree commands (`dependencyTree`, etc.) are **built into sbt 2** — the old `addDependencyTreePlugin` is no longer required

### Plugins from other sources
```scala
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("com.github.sbt" % "sbt-header" % "5.11.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.7.0")
```
* [sbt-buildinfo](https://github.com/sbt/sbt-buildinfo) - Your program can know all kinds of things about your build
* [sbt-header](https://github.com/sbt/sbt-header) - Keep those file headers up to date with your project license (now published under `com.github.sbt`)
* [sbt-updates](https://github.com/rtimush/sbt-updates) - Check for dependency updates

> **Dropped on sbt 2:** `sbt-sonatype` (deprecated — Maven Central publishing is
> built into sbt 2 via the Central Portal), `sbt-github-packages` (abandoned —
> GitHub Packages is configured directly via `publishTo`/`Credentials`), and
> `sbt-paradox` (no stable sbt 2 release yet). See Publishing helpers and the
> Migration Notes below.

### Scala Specific Plugins
```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.7")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.4.4")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.12")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.22.0")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.6")
```
* [sbt-scalafix](https://github.com/scalacenter/sbt-scalafix) - Code refactoring and linting
* [sbt-scalafmt](https://github.com/scalameta/sbt-scalafmt) - Code formatting
* [sbt-scoverage](https://github.com/scoverage/sbt-scoverage) - Code coverage measurement
* [sbt-scala-native](https://github.com/scala-native/scala-native) - Compile Scala to native code
* [sbt-scalajs](https://github.com/scala-js/scala-js) - Compile Scala to JavaScript
* [sbt-mima-plugin](https://github.com/lightbend-labs/mima) - Binary compatibility checking

> **Obsolete / unavailable on sbt 2:** the `org.portable-scala` cross-project
> plugins and `sbt-platform-deps` are replaced by sbt 2's built-in `projectMatrix`
> and the `%%` operator (the `%%%` operator is gone). `sbt-coveralls`,
> `sbt-tasty-mima`, and `sbt-idea-plugin` have no sbt 2 release yet:
> `With.coverage`/`With.MiMa` work without them, but `With.IdeaPlugin` is
> unavailable until JetBrains ships sbt 2 support.


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
* Native sbt 2 Maven Central publishing via the Sonatype Central Portal
* sbt-scoverage for code coverage measurement

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
  startYr = 2024,
  projectId = "root"  // Optional: customize sbt project ID (default: "root")
)
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
lazy val foo_cp: CrossModule =
  CrossModule(dirName = "foo", modName = "foo", scalaVersion = "3.8.4")(JVM, JS, Native)
    .configure(With.typical, With.publishing)
    .settings(
      scalacOptions ++= Seq("-explain", "-explain-cyclic"),
      description := "The fooness of existence"
    )
    .jvmConfigure(With.coverage(30))
    .jvmSettings(
      coverageExcludedPackages := "<empty>;$anon"
    )
    .jsConfigure(With.ScalaJS("foo: js", withCommonJSModule = true))
    .jsSettings(
      libraryDependencies += "com.foo" %% "fooness" % "0.1.0"
    )
val foo       = foo_cp.jvm
val fooJS     = foo_cp.js
val fooNative = foo_cp.native
```

On **sbt 2**, `CrossModule` is implemented on sbt's built-in **`projectMatrix`** (which
replaces the sbt 1.x `org.portable-scala` cross-project plugins). From top to bottom:

* a lazy val named `foo_cp` is defined as a `CrossModule` — an immutable builder wrapping a
  `projectMatrix`. It is named with the `_cp` suffix to distinguish it from the per-platform
  projects extracted below.
* The `CrossModule` object is invoked. The first arguments are like a `Module` (`dirName`,
  `modName`), plus a **`scalaVersion`** (default `"3.8.4"`): `projectMatrix` needs the Scala
  version when each platform is declared, and it must match the version configured by
  `With.typical`/`With.Scala3` on this module. The trailing argument list selects the targets to
  build — here all three: `JVM`, `JS`, and `Native`.
* `.configure` and `.settings` apply to **all** platforms. Doing something specific to one (like a
  JS-only `libraryDependency`) here would break the JVM build.
* `.jvmConfigure`/`.jvmSettings` apply only to the JVM build; `.jsConfigure`/`.jsSettings` only to
  the Scala.js build; `.nativeConfigure`/`.nativeSettings` only to the Scala Native build.
* Cross-platform dependencies use the ordinary **`%%`** operator (sbt 2 cross-builds it across
  platforms; the sbt 1.x `%%%` operator is gone).
* The terse vals `foo`, `fooJS`, and `fooNative` extract the per-platform sub-projects (so you can,
  e.g., run `fooJS/test`). `.jvm`/`.js`/`.native` resolve `matrix.{jvm,js,native}.apply(scalaVersion)`.

### Plugin

Use this to define an SBT plugin in a sub-project, like this:

```scala
lazy val plugin = Plugin(dirName = "sbt-plugin")
  .configure(With.BuildInfo)
  .settings(
    description := "An sbt plugin to help the build world along",
    buildInfoObject := "SbtRiddlPluginBuildInfo",
    buildInfoPackage := "com.ossuminc.riddl.sbt",
    buildInfoUsePackageAsPath := true
  )
```

In this example we define that the `sbt-plugin` is in the eponymous directory and add BuildInfo
generation. On **sbt 2** a plugin compiles with **Scala 3** (managed by sbt — do **not** set
`With.Scala2`/`scalaVersion := "2.12.x"`), and `Plugin(...)` automatically configures GitHub
Packages publishing plus the `scripted` testing framework (built into sbt 2) for you.

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
* **`With.ScalaJavaTime()`** - Add `scala-java-time` dependency for cross-platform `java.time` API
* **`With.ClassPathJar`** - Use classpath JAR for packaging (reduces command line length)
* **`With.UnmanagedJars`** - Use unmanaged JAR files from `libs/` directory
* **`With.ShellPrompt`** - Custom shell prompt showing project name, git branch, and version
* **`With.release`** - Enable `sbt-release` plugin
* **`With.resolvers`** - Add standard resolvers (Maven Local, JCenter, Typesafe)
* **`With.scala2`** - Configure for Scala 2.13 (latest)
* **`With.scala3`** - Configure for Scala 3.8.4 (Scala Next, default)
* **`With.ScalaCoverage`** (a.k.a. `With.coverage(...)`) - Enable code coverage with sbt-scoverage

### Publishing Helpers

* **`With.Publishing`** - Configure publishing (defaults to GitHub Packages)
* **`With.Publishing.github`** - Explicitly configure GitHub Packages publishing
* **`With.Publishing.sonatype`** - Configure publishing to Sonatype/Maven Central
* **`With.GithubPublishing`** - Alias for `With.Publishing.github`
* **`With.SonatypePublishing`** - Alias for `With.Publishing.sonatype`
* **`With.Publishing.npm(...)`** - Publish npm packages to registries (see
  [npm Publishing](#withpublishingnpm) below)

> **Note**: Do not combine GitHub and Sonatype publishing in the same project.

> **Maven Central (Central Portal):** `With.Publishing.sonatype` configures sbt 2's
> native Central Portal publishing — snapshots go to the Central snapshots repo,
> releases to the built-in `localStaging`. Release with
> `publishSigned ; sonaUpload ; sonaRelease`, providing `SONATYPE_USERNAME` /
> `SONATYPE_PASSWORD` (env) or `~/.sbt/sonatype_central_credentials`, plus PGP
> signing keys. The legacy `sbt-sonatype` plugin is not used (and has no sbt 2 build).

### Composite Helpers

Shortcuts that combine multiple helpers:

* **`With.basic`** - Combines: `aliases`, `dynver`, `git`, `header`, `resolvers`
* **`With.typical`** - Combines: `basic`, `scala3`, `Scalatest()`
* **`With.everything`** - Combines: `typical`, `java`, `release`

### Parameterized Configuration Helpers

These helpers accept parameters for customization:

#### **`With.Akka.forRelease(...)`**
Add Akka dependencies to the project. Akka requires a commercial license and repository token since 2024.

**Parameters:**
- **`release`**: Akka version (`"24.10"` or `"25.10"` (latest), default: `""` = latest)
- **`withHTTP`**: Include Akka HTTP modules (default: `false`)
- **`withGrpc`**: Include Akka gRPC runtime (default: `false`)
- **`withPersistence`**: Include Akka Persistence R2DBC (default: `false`)
- **`withProjections`**: Include Akka Projections (default: `false`)
- **`withManagement`**: Include Akka Management core (health checks, cluster HTTP) (default: `false`)
- **`withManagementKubernetes`**: Include Kubernetes modules (discovery, lease, rolling updates) (default: `false`)
- **`withKafka`**: Include Alpakka Kafka connector (default: `false`)
- **`withInsights`**: Include Akka Insights/Cinnamon telemetry (default: `false`)
- **`withInsightsPrometheus`**: Include Prometheus export (default: `true`, only applies when `withInsights = true`)
- **`withInsightsOpenTelemetry`**: Include OpenTelemetry tracing (default: `true`, only applies when `withInsights = true`)

**Basic usage (core modules only):**
```scala
Module("my-actor-system")
  .configure(With.Akka.forRelease("25.10"))
```

**Full-featured server example:**
```scala
Module("my-server")
  .configure(With.Akka.forRelease(
    "25.10",
    withHTTP = true,
    withPersistence = true,
    withProjections = true,
    withManagement = true,
    withManagementKubernetes = true,
    withInsights = true
  ))
```

**Modules included by default (core):**
- akka-actor, akka-actor-typed
- akka-cluster, akka-cluster-typed, akka-cluster-sharding, akka-cluster-sharding-typed, akka-cluster-tools
- akka-coordination, akka-discovery, akka-distributed-data
- akka-persistence, akka-persistence-typed, akka-persistence-query
- akka-remote, akka-serialization-jackson, akka-slf4j
- akka-stream, akka-stream-typed
- Test: akka-testkit, akka-actor-testkit-typed, akka-stream-testkit

> **Note**: Akka repository access requires `AKKA_REPO_TOKEN` environment variable.
> Get your token at https://account.akka.io

> **Note for riddl-server-infrastructure dependents:** If your project depends on
> `riddl-server-infrastructure`, Akka core and HTTP modules are already provided
> transitively. Only use `With.Akka.forRelease()` if you need additional modules
> beyond what the server infrastructure provides (e.g., Kafka, Insights, Management,
> Projections, or specific persistence backends).

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

> **Unavailable on sbt 2** until JetBrains' `sbt-idea-plugin` ships an sbt 2.0
> build ([SCL-23480](https://youtrack.jetbrains.com/issue/SCL-23480)). On sbt 2,
> calling `With.IdeaPlugin` fails fast with a clear message — use the sbt-ossuminc
> **1.x** line for IntelliJ plugin projects until then.

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
- **`scalaJavaTimeVersion`**: Override scala-java-time version (default: `"2.6.0"`)
- **`scalatestVersion`**: Override scalatest version (default: `"3.2.19"`)

```scala
CrossModule("my-ui", "ui")(JVM, JS)
  .jsConfigure(With.ScalaJS(
    header = "My App UI v1.0",
    hasMain = true,
    forProd = true,
    scalaJavaTimeVersion = "2.6.0"
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
- **`scalatestVersion`**: Override scalatest version (default: `"3.2.19"`)

```scala
CrossModule("cli-tool", "tool")(JVM, Native)
  .nativeConfigure(With.Native(
    mode = "release",
    buildTarget = "application",
    scalatestVersion = "3.2.19"
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

#### **`With.Packaging.dockerDual(...)`**
Create separate Docker images for development (local) and production (GKE/cloud).

**Parameters:**
- **`mainClass`**: Fully qualified main class name (e.g., `"com.myapp.Main"`)
- **`pkgName`**: Docker image name (e.g., `"my-service"`)
- **`exposedPorts`**: Ports to expose in the container
- **`pkgDescription`**: Optional image description

**Dev image** (default, built with `docker:publishLocal`):
- Base: `eclipse-temurin:25-jdk-noble` (Ubuntu 24.04 with JDK tools)
- Architecture: Host platform (arm64 on Apple Silicon, amd64 on Intel/Linux)
- Tags: `:dev-latest`, `:dev-<version>`
- Includes JDK diagnostic tools (jcmd, jstack, jmap) for debugging

**Prod image** (built with `dockerPublishProd`):
- Base: `gcr.io/distroless/java25-debian13:nonroot` (minimal, secure)
- Architecture: `linux/amd64` (for GKE/cloud deployment)
- Tags: `:latest`, `:<version>`
- Minimal attack surface, no shell, runs as non-root

```scala
Module("my-service", "service")
  .configure(With.typical)
  .configure(
    With.Packaging.dockerDual(
      mainClass = "com.myapp.Main",
      pkgName = "my-service",
      exposedPorts = Seq(8080, 9001)
    )
  )
```

**Building images:**
```bash
# Build dev image for local testing
sbt docker:publishLocal

# Build and push prod image to registry (requires docker buildx)
sbt dockerPublishProd
```

**Default registry:** Google Artifact Registry
(`us-central1-docker.pkg.dev` / `ossuminc-production/ossum-images`); override with
the `dockerRepository` and `dockerUsername` settings.

#### **`With.Packaging.npm(...)`**
Assemble Scala.js output into an npm-publishable package. Requires the project
to be configured with Scala.js (`With.ScalaJS(...)` or a `CrossModule` with JS
target).

**Parameters:**
- **`scope`**: npm scope (e.g., `"@ossuminc"`), empty string for unscoped
- **`pkgName`**: npm package name (without scope)
- **`pkgDescription`**: Description for package.json
- **`keywords`**: npm keywords for package discovery
- **`esModule`**: Whether to set `"type": "module"` in package.json (default: `true`)
- **`templateFile`**: Optional `package.json` template with `VERSION_PLACEHOLDER`

**Tasks provided:**
- **`npmPrepare`**: Assembles the npm package directory (pure sbt, no npm
  binary required). Runs `fullOptJS`, copies JS output, generates
  `package.json`, and includes TypeScript definitions if found.
- **`npmPack`**: Shells out to `npm pack` to create a `.tgz` archive.

**TypeScript definitions** are discovered by convention: if
`<project-base>/types/index.d.ts` exists, it is copied into the package and
referenced in `package.json`.

```scala
CrossModule("my-lib", "my-lib")(JVM, JS)
  .jsConfigure(
    With.ScalaJS("My Lib", hasMain = false, forProd = true,
      withCommonJSModule = true),
    With.Packaging.npm(
      scope = "@myorg",
      pkgName = "my-lib",
      pkgDescription = "My library for JavaScript",
      keywords = Seq("scala", "library"),
      esModule = true
    )
  )
```

**Template mode**: Instead of generating `package.json` from settings, you
can provide a template file containing `VERSION_PLACEHOLDER` which will be
replaced with the project version at build time:

```scala
With.Packaging.npm(
  scope = "@myorg",
  pkgName = "my-lib",
  templateFile = Some(file("npm/package.json.template"))
)
```

#### **`With.Publishing.npm(...)`**
Publish assembled npm packages to registries. Must be used together with
`With.Packaging.npm(...)` which provides the `npmPrepare` task.

**Parameters:**
- **`registries`**: Target registries — `Seq("npmjs")`, `Seq("github")`,
  or `Seq("npmjs", "github")` to publish to both.

**Tasks provided:**
- **`npmPublish`**: Publish to all configured registries.
- **`npmPublishNpmjs`**: Publish to npmjs.com only.
- **`npmPublishGithub`**: Publish to GitHub Packages only.

**Authentication** via environment variables:
- **`NPM_TOKEN`**: Access token for npmjs.com (required for `"npmjs"` registry)
- **`GITHUB_TOKEN`**: Token with `write:packages` scope (required for
  `"github"` registry)

> **Note**: GitHub Packages requires the npm scope to be set (e.g.,
> `@ossuminc`). The scope is read from `With.Packaging.npm(scope = ...)`.

```scala
CrossModule("my-lib", "my-lib")(JVM, JS)
  .jsConfigure(
    With.Packaging.npm(
      scope = "@myorg",
      pkgName = "my-lib",
      pkgDescription = "My library"
    ),
    With.Publishing.npm(
      registries = Seq("npmjs", "github")
    )
  )
```

**Typical workflow:**
```bash
# Build and assemble npm package (pure sbt, no npm needed)
sbt my-libJS/npmPrepare

# Publish to configured registries
NPM_TOKEN=<token> GITHUB_TOKEN=<token> sbt my-libJS/npmPublish
```

#### **`With.Packaging.linux(...)`**
Create a tar.gz archive of a Scala Native binary for distribution. The
archive includes the binary and optionally README and LICENSE files.

**Parameters:**
- **`pkgName`**: Base name for the archive and binary
- **`pkgDescription`**: Package description (included in generated README)
- **`arch`**: Architecture label override (empty = auto-detect from host)
- **`os`**: OS label override (empty = auto-detect from host)
- **`includeReadme`**: Whether to include a README.md (default: `true`)
- **`includeLicense`**: Whether to include LICENSE from project root
  (default: `true`)

**Task provided:**
- **`linuxPackage`**: Compiles via `nativeLink`, stages binary + docs,
  creates `<pkgName>-<version>-<os>-<arch>.tar.gz`.

OS and architecture are auto-detected from the build host since Scala Native
compiles for the host platform only. For multi-platform distribution, use CI
matrix runners for each target.

```scala
Program("my-tool", "my-tool", Some("com.myapp.Main"))
  .nativeConfigure(With.Native(mode = "release"))
  .configure(
    With.Packaging.linux(
      pkgName = "my-tool",
      pkgDescription = "A useful CLI tool"
    )
  )
```

#### **`With.Packaging.homebrew(...)`**
Generate a Homebrew formula `.rb` file for inclusion in a tap repository.

**Parameters:**
- **`formulaName`**: Formula name (used as Ruby class name)
- **`binaryName`**: Binary executable name
- **`pkgDescription`**: Description shown in `brew info`
- **`homepage`**: Project homepage URL
- **`javaVersion`**: Required JDK version, universal variant only
  (default: `"25"`)
- **`tapRepo`**: Tap repo path (documentation only, publishing is not
  automated)
- **`variant`**: `"universal"` (JVM, default) or `"native"` (Scala Native)

**Task provided:**
- **`homebrewGenerate`**: Produces a `.rb` formula file at
  `target/homebrew/Formula/<formulaName>.rb` with SHA256 hash computed
  from the build artifact.

**Variants:**
- `"universal"`: Depends on `Universal/packageBin` (`.zip`). Formula includes
  `depends_on "openjdk@<version>"`.
- `"native"`: Depends on `linuxPackage` (`.tar.gz` from
  `With.Packaging.linux(...)`). No JDK dependency.

```scala
// JVM universal variant
Program("my-tool", "my-tool", Some("com.myapp.Main"))
  .configure(
    With.Packaging.universal(
      maintainerEmail = "dev@example.com",
      pkgName = "my-tool",
      pkgSummary = "My Tool",
      pkgDescription = "A useful tool"
    ),
    With.Packaging.homebrew(
      formulaName = "my-tool",
      binaryName = "my-tool",
      pkgDescription = "A useful tool",
      homepage = "https://example.com"
    )
  )
```

Publishing the generated formula to a tap is a separate operation — copy the
`.rb` file to your tap repository (e.g., `ossuminc/homebrew-tap`).

#### **`With.Packaging.windowsMsi(...)`** *(placeholder)*
Reserved for future Windows MSI installer packaging. Currently logs a warning
and returns the project unchanged. Not yet implemented.

#### **`With.Packaging.graalVM(...)`**
Create GraalVM native images.
- **`pkgName`**: Executable name
- **`pkgSummary`**: Summary
- **`native_image_path`**: Path to native-image executable

#### **`With.Riddl(...)`**
Add RIDDL library dependencies.
- **`version`**: RIDDL version to use
- **`nonJVM`**: marks the dependency as cross-platform (JS/Native as well as JVM).
  On sbt 2 the `%%` operator cross-builds across platforms (the `%%%` operator is gone).

```scala
Module("my-riddl-app")
  .configure(With.Riddl(version = "0.50.0"))
```

#### **`With.Scala3(...)`**
Configure Scala 3 with custom version, compiler options, and documentation.
- **`version`**: Scala version (defaults to `"3.8.4"`)
- **`scala3Options`**: Additional compiler options
- **`projectName`**: Project name for scaladoc output (optional)
- **`docSiteRoot`**: Root directory for documentation site (optional)
- **`docBaseURL`**: Base URL for API documentation (optional)

```scala
Module("my-experimental")
  .configure(With.Scala3(
    version = Some("3.4.0"),
    scala3Options = Seq("-experimental")
  ))

// With documentation settings
Module("my-lib")
  .configure(With.Scala3(
    projectName = Some("MyLib"),
    docSiteRoot = Some("docs/api"),
    docBaseURL = Some("https://myproject.org/api")
  ))
```

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

## Migration Notes

### Migrating to 2.0.0 (sbt 1.x → sbt 2)

sbt-ossuminc **2.0.0** requires **sbt 2.0.2+** and **Scala 3**; the 1.x line
(v1.4.0) remains for sbt 1.x builds. To upgrade a consuming project:

1. **Bump sbt**: set `sbt.version=2.0.2` (the minimum; newer is fine) in
   `project/build.properties`, and the plugin to `2.0.0` in
   `project/plugins.sbt`.
2. **Credentials**: move `~/.sbt/1.0/github.sbt` to `~/.sbt/2/github.sbt`.
3. **Cross-platform modules**: `CrossModule(...)` now takes a `scalaVersion`
   argument and is built on the built-in `projectMatrix`. Replace `%%%` with
   `%%` in cross-platform dependencies. The `.jvm`/`.js`/`.native`, `.configure`,
   `.settings`, and `.{jvm,js,native}{Configure,Settings}` API is otherwise
   unchanged.
4. **sbt plugins** (`Plugin(...)`): drop `With.Scala2` and any
   `scalaVersion := "2.12.x"` — sbt 2 plugins compile with Scala 3 (managed by sbt).
5. **Publishing**: GitHub Packages is unchanged. Maven Central now uses sbt 2's
   native Central Portal (see Publishing helpers) — no `sbt-sonatype`.
6. **Unavailable until upstream ships sbt 2 builds**: `With.IdeaPlugin` (JetBrains
   sbt-idea-plugin), Coveralls upload, TASTy-MiMa, and stable sbt-paradox docs.
   The corresponding helpers degrade gracefully or fail fast with a clear message.

No special configuration is needed for the transitive scala-xml /
scala-collection-compat cross-version clash: sbt-ossuminc's published POM excludes
the Scala 2.13 variants so your meta-build resolves cleanly.

### Migrating from 1.1.0 to 1.2.0

#### Breaking Change: CrossModule Dependencies Now Opt-In

In 1.1.0, `CrossModule` automatically included testing and time dependencies. In 1.2.0, these are now opt-in for cleaner, more explicit builds.

```scala
// Old (1.1.0) - dependencies were automatic
CrossModule("foo", "bar")(JVM, JS)

// New (1.2.0) - explicitly add what you need
CrossModule("foo", "bar")(JVM, JS)
  .configure(With.Scalatest())      // Add if you need testing
  .configure(With.ScalaJavaTime())  // Add if you need java.time API
```

#### New Helpers in 1.2.0

* **`With.Publishing`** - Generic publishing helper (defaults to GitHub Packages)
  - Use `With.Publishing` for default (GitHub) publishing
  - Use `With.Publishing.github` to explicitly use GitHub Packages
  - Use `With.Publishing.sonatype` for Sonatype/Maven Central

* **`With.ScalaJavaTime()`** - Add `scala-java-time` dependency for cross-platform date/time support

* **`With.ClassPathJar`** - Use classpath JAR to reduce command line length on Windows

* **`With.UnmanagedJars`** - Configure unmanaged JAR files from `libs/` directory

* **`With.ShellPrompt`** - Custom sbt shell prompt with project, branch, and version info

#### Other Changes

* **`Root()` project ID is now configurable** - Use `projectId` parameter to customize (default: `"root"`)
* **Parameterized versions** - `With.ScalaJS()` and `With.Native()` now accept version parameters to override defaults

### New in 1.3.0

#### Packaging Helpers

* **`With.Packaging.npm(...)`** - Assemble Scala.js output into npm packages.
  Tasks: `npmPrepare` (pure sbt) and `npmPack` (shells out to npm).
  Supports template mode and TypeScript definition auto-discovery.

* **`With.Packaging.linux(...)`** - Create tar.gz archives of Scala Native
  binaries. Auto-detects host OS and architecture. Task: `linuxPackage`.

* **`With.Packaging.homebrew(...)`** - Generate Homebrew formula `.rb` files.
  Supports `"universal"` (JVM) and `"native"` (Scala Native) variants.
  Task: `homebrewGenerate`.

* **`With.Packaging.windowsMsi(...)`** - Placeholder for future Windows MSI
  support (not yet implemented).

#### Publishing Helpers

* **`With.Publishing.npm(...)`** - Publish npm packages to npmjs.com and/or
  GitHub Packages. Auth via `NPM_TOKEN` and `GITHUB_TOKEN` env vars.
  Tasks: `npmPublish`, `npmPublishNpmjs`, `npmPublishGithub`.


