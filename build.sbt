/*
 * Copyright 2015-2025, Ossum Inc. All Rights Reserved.
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

import com.ossuminc.sbt.helpers.{DynamicVersioning, RootProjectInfo, Scala2, SonatypePublishing}

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val `sbt-ossuminc` = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .configure(RootProjectInfo.initialize("sbt-ossuminc", startYr = 2015))
  .configure(DynamicVersioning.configure)
  .configure(Scala2.configure)
  .configure(SonatypePublishing.configure)
  .settings(
    licenses += "Apache V2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"),
    name := "sbt-ossuminc",
    scalaVersion := "2.12.19",
    // Scripted == sbt plugin tests
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.15.0",
      "org.slf4j" % "slf4j-simple" % "2.0.13"
    )
  )

// Generic plugins from github.sbt project
addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.0.1")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.4")
addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.1")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")

// Helpers from other sources
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.12.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.10.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.11.1")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox" % "0.9.2")

// Scala specific from various places
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.12.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.1.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.14")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.6")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.17.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.4")
addSbtPlugin("ch.epfl.scala" % "sbt-tasty-mima" % "1.2.0")
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta44")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.6")
