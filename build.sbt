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

import java.time.Year
import com.ossuminc.sbt.helpers.{ProjectInfo, RootProject, Scala2}
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.headerLicense

Global / onChangedBuildSource := ReloadOnSourceChanges

sbtPlugin := true

val plugin = RootProject("sbt-ossuminc")
  .configure(ProjectInfo.configure)
  .configure(Scala2.configure)
  .enablePlugins(ScriptedPlugin, HeaderPlugin, SbtPgp)
  .settings(
    scalaVersion := "2.12.18",
    // Scripted - sbt plugin tests
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    logLevel := Level.Info,
    resolvers ++= Seq(
      // Resolver.sonatypeRepo("releases"),
      // Resolver.sonatypeRepo("snapshots"),
      Resolver.bintrayRepo("sbt", "sbt-plugin-releases"),
      Resolver.typesafeIvyRepo("releases"),
      "eclipse-jgit".at("https://download.eclipse.org/jgit/maven")
    ),
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.5",
      "org.slf4j" % "slf4j-simple" % "1.7.25"
    ),
    startYear := Some(2015),
    headerLicense := {
      val years = startYear.value.get.toString + "-" + Year.now().toString
      Some(HeaderLicense.ALv2(years, "Ossum Inc."))
    },
    // Publishing to sonatype
    // Sonatype.SonatypeKeys.sonatypeProfileName := "com.reactific"
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      val snapshotsR =
        "snapshots".at(nexus + "content/repositories/snapshots")
      val releasesR =
        "releases".at(nexus + "service/local/staging/deploy/maven2")
      val resolver = if (isSnapshot.value) snapshotsR else releasesR
      Some(resolver)
    },
    publishMavenStyle := true,
    Test / publishArtifact := false,
    pomIncludeRepository := { _ => false }
  )

lazy val defaultScmInfo = Def.setting {
  val gitUrl = "//github.com/ossuminc/" + normalizedName.value + ".git"
  ScmInfo(
    url("https:" ++ gitUrl),
    "scm:git:" ++ gitUrl,
    Some("https:" ++ gitUrl)
  )
}

// Release process
//releaseUseGlobalVersion := true
//releaseVersionBump := sbtrelease.Version.Bump.Bugfix
//releasePublishArtifactsAction := PgpKeys.publishSigned.value
//releaseProcess := Seq[ReleaseStep](
//  checkSnapshotDependencies,
//  inquireVersions,
//  runClean,
//  runTest,
//  releaseStepCommand("scripted"),
//  setReleaseVersion,
//  commitReleaseVersion,
//  tagRelease,
//  releaseStepCommand("packageBin"),
//  publishArtifacts,
//  setNextVersion,
//  commitNextVersion,
//  releaseStepCommand("sonatypeReleaseAll"),
//  pushChanges
//)

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.2")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.5")
// addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")
// addSbtPlugin("com.github.sbt" % "sbt-release" % "1.0.8")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.7")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.4")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.2")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.0")
addSbtPlugin("org.jetbrains" % "sbt-ide-settings" % "1.0.0")
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.1.5")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
