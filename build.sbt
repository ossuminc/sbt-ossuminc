/*
 * Copyright 2015-2026 Ossum Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed  on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for  the specific language governing permissions and limitations under the License.
 */

// ============================================================================
// sbt 2.0 build for sbt-ossuminc.
//
// Unlike the sbt 1.x build, this does NOT self-bootstrap via project/ symlinks
// to its own helpers. sbt 2 meta-builds use Scala 3 and the new task/file APIs,
// which makes the old symlink approach impractical. Project metadata, dynamic
// versioning, and publishing are configured inline here instead of through the
// plugin's own With.* helpers. See NOTEBOOK.md "sbt 2.0 Migration Plan".
// ============================================================================

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val `sbt-ossuminc` = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    // Project metadata (was RootProjectInfo helper)
    organization := "com.ossuminc",
    organizationName := "Ossum Inc.",
    organizationHomepage := Some(url("https://ossuminc.com/")),
    name := "sbt-ossuminc",
    description := "SBT plugin providing build infrastructure for Ossum Inc. projects",
    homepage := Some(url("https://github.com/ossuminc/sbt-ossuminc")),
    licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("reid-spencer", "Reid Spencer", "", url("https://github.com/reid-spencer"))
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/ossuminc/sbt-ossuminc"),
        "scm:git:https://github.com/ossuminc/sbt-ossuminc.git"
      )
    ),

    // Plugin settings
    sbtPlugin := true,
    scalaVersion := "3.8.4", // matches the Scala version sbt 2.0.0 ships

    // Dynamic versioning (was DynamicVersioning helper). These MUST be at
    // ThisBuild scope: sbt-dynver computes ThisBuild/version using the ThisBuild
    // values. At project scope they are ignored, dynver defaults to expecting a
    // "v" tag prefix, and a tag like "2.0.0" is not matched (-> 0.0.0+<n> fallback).
    ThisBuild / dynverSeparator := "-",
    ThisBuild / dynverVTagPrefix := false,

    // Scripted == sbt plugin tests
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value) ++
        sys.env.get("AKKA_REPO_TOKEN").map(t => s"-Dakka.repo.token=$t").toSeq ++
        sys.env.get("GITHUB_TOKEN").map(t => s"-Dgithub.token=$t").toSeq
    },

    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.20.0",
      "org.slf4j" % "slf4j-simple" % "2.0.17"
    ),

    // Resolve transitive cross-version (_2.13 vs _3) conflicts in the
    // Scala 3 metabuild by excluding the Scala 2.13 variants and relaxing
    // the version scheme. A plugin in the re-export set drags in 2.13 copies.
    libraryDependencySchemes ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
      "org.scala-lang.modules" %% "scala-collection-compat" % VersionScheme.Always
    ),
    excludeDependencies ++= Seq(
      "org.scala-lang.modules" % "scala-xml_2.13",
      "org.scala-lang.modules" % "scala-collection-compat_2.13"
    ),

    // Publish to GitHub Packages. On sbt 1.x this was done via
    // .configure(GithubPublishing) through project/ self-bootstrapping, which is
    // removed on sbt 2, so it is inlined here. Credentials come from GITHUB_TOKEN
    // (CI) or the global ~/.sbt/2/github.sbt file (local).
    publishMavenStyle := true,
    publishTo := Some(
      "GitHub Packages" at "https://maven.pkg.github.com/ossuminc/sbt-ossuminc"
    ),
    credentials ++= {
      val token = sys.env.get("GITHUB_TOKEN").orElse(sys.props.get("github.token"))
      val user = sys.env.getOrElse("GITHUB_ACTOR", "_")
      token
        .map(t => Credentials("GitHub Package Registry", "maven.pkg.github.com", user, t))
        .toSeq
    }
  )

// ============================================================================
// Plugins RE-EXPORTED to consumers of sbt-ossuminc (transitive dependencies).
//
// Versions verified to have sbt 2.0 (_sbt2_3) artifacts on Maven Central as of
// 2026-06. See NOTEBOOK.md "sbt 2.0 Migration Plan" for the full categorization.
// ============================================================================
// sbt 2 transition shield: a few re-exported plugins still drag in the Scala
// 2.13 copies of scala-xml / scala-collection-compat via deep transitives,
// which clash with the Scala 3 metabuild ("Conflicting cross-version suffixes").
// Excluding the _2.13 variants here bakes the exclusions into sbt-ossuminc's
// published POM, so CONSUMERS inherit them and don't each have to add the
// exclusion to their own project/plugins.sbt. (The _3 variants still resolve.)
def reexport(m: ModuleID): ModuleID =
  m.exclude("org.scala-lang.modules", "scala-xml_2.13")
    .exclude("org.scala-lang.modules", "scala-collection-compat_2.13")

addSbtPlugin(reexport("com.github.sbt" % "sbt-dynver" % "5.1.1"))
addSbtPlugin(reexport("com.github.sbt" % "sbt-native-packager" % "1.11.7"))
addSbtPlugin(reexport("com.github.sbt" % "sbt-git" % "2.1.0"))
addSbtPlugin(reexport("com.github.sbt" % "sbt-pgp" % "2.3.1"))
addSbtPlugin(reexport("com.github.sbt" % "sbt-release" % "1.5.0"))
addSbtPlugin(reexport("com.github.sbt" % "sbt-unidoc" % "0.6.1"))
addSbtPlugin(reexport("com.eed3si9n" % "sbt-buildinfo" % "0.13.1"))
addSbtPlugin(reexport("com.github.sbt" % "sbt-header" % "5.11.0")) // org moved from de.heikoseeberger
addSbtPlugin(reexport("com.timushev.sbt" % "sbt-updates" % "0.7.0"))
addSbtPlugin(reexport("ch.epfl.scala" % "sbt-scalafix" % "0.14.7"))
addSbtPlugin(reexport("org.scalameta" % "sbt-scalafmt" % "2.5.6"))
addSbtPlugin(reexport("org.scoverage" % "sbt-scoverage" % "2.4.4"))
addSbtPlugin(reexport("org.scala-native" % "sbt-scala-native" % "0.5.12"))
addSbtPlugin(reexport("org.scala-js" % "sbt-scalajs" % "1.22.0"))
addSbtPlugin(reexport("com.typesafe" % "sbt-mima-plugin" % "1.1.6"))

// ----------------------------------------------------------------------------
// Dropped for sbt 2.0 (see NOTEBOOK.md "sbt 2.0 Migration Plan"):
//
// Absorbed / obsoleted by sbt 2 core:
//   - addDependencyTreePlugin           -> dependency-tree is built in
//   - scripted-plugin dependency        -> bundled with SbtPlugin
//   - sbt-scalajs-crossproject          -> projectMatrix (in core)
//   - sbt-scala-native-crossproject     -> projectMatrix (in core)
//   - sbt-platform-deps                 -> core %% + `platform` (no more %%%)
//
// Superseded (no sbt 2 build; replacement exists):
//   - sbt-sonatype (deprecated)         -> native Central Portal (sonaUpload/Release)
//   - sbt-github-packages (abandoned)   -> plain publishTo + Credentials
//
// Blocked: no sbt 2.0 artifact yet (helpers degraded until upstream ships):
//   - sbt-coveralls    (coverage upload; sbt-scoverage measurement still works)
//   - sbt-tasty-mima   (TASTy checks; binary sbt-mima-plugin still works)
//   - sbt-paradox      (only 0.11.0-M4 milestone; docs site)
//   - sbt-idea-plugin  (blocks IdeaPlugin helper + riddl-idea-plugin consumer)
// ----------------------------------------------------------------------------
