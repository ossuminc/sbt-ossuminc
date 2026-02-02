/*
 * Copyright 2023 Ossum Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Generic plugins from github.sbt project - sbt 2.x compatible
addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.1.1")

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.3")

addSbtPlugin("com.github.sbt" % "sbt-git" % "2.1.0")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")

// sbt-release: awaiting sbt 2.0 support
// addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")

// sbt-unidoc: awaiting sbt 2.0 support
// addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")

// addDependencyTreePlugin is built-in to sbt 2.x

// Helpers from other sources - sbt 2.x compatible
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")

// sbt-header: awaiting sbt 2.0 support
// addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.11.0")

// sbt-updates: awaiting sbt 2.0 support
// addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

// sbt-sonatype: awaiting sbt 2.0 support
// addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.12.0")

// sbt-github-packages: awaiting sbt 2.0 support
// addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")

// sbt-paradox: awaiting sbt 2.0 support
// addSbtPlugin("com.lightbend.paradox" % "sbt-paradox" % "0.9.2")

// Scala specific from various places

// sbt-scalafix: awaiting sbt 2.0 support
// addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.5")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")

// sbt-scoverage: awaiting sbt 2.0 support
// addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.4.1")

// sbt-coveralls: awaiting sbt 2.0 support
// addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.15")

// sbt-scala-native: awaiting sbt 2.0 support
// addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.9")

// sbt-scalajs: awaiting sbt 2.0 support
// addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.20.1")

// sbt-scalajs-crossproject: awaiting sbt 2.0 support
// addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")

// sbt-scala-native-crossproject: awaiting sbt 2.0 support
// addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")

// sbt-platform-deps: awaiting sbt 2.0 support
// addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.2")

// sbt-mima-plugin: awaiting sbt 2.0 support
// addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.4")

// sbt-tasty-mima: awaiting sbt 2.0 support
// addSbtPlugin("ch.epfl.scala" % "sbt-tasty-mima" % "1.2.0")

// sbt-converter (scalably-typed): awaiting sbt 2.0 support
// addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta44")

// sbt-idea-plugin: awaiting sbt 2.0 support
// addSbtPlugin("org.jetbrains.scala" % "sbt-idea-plugin" % "5.0.4")

// Note: In sbt 2.x, scripted-plugin is included with SbtPlugin

// This removes a version conflict
ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

// Exclude Scala 2.13 version of scala-xml that comes from coursier transitively
ThisBuild / excludeDependencies += "org.scala-lang.modules" % "scala-xml_2.13"
