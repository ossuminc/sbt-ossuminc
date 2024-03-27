/*
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.
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
  .enablePlugins(ScriptedPlugin)
  .configure(RootProjectInfo.initialize("sbt-ossuminc", startYr = 2015))
  .configure(DynamicVersioning.configure)
  .configure(Scala2.configure)
  .configure(SonatypePublishing.configure)
  .settings(
    sbtPlugin := true,
    name := "sbt-ossuminc",
    scalaVersion := "2.12.18",
    // Scripted == sbt plugin tests
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++ Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.5",
      "org.slf4j" % "slf4j-simple" % "1.7.25"
    )
  )

// Generic plugins from github.sbt project
addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.0.1")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")

// Helpers from other sources
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.10.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.10.0")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")

// Scala specific from various places
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.11.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.11")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.16")
