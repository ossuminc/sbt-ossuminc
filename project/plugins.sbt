/*
 * Copyright 2023-2026 Ossum Inc.
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

// Plugins used to BUILD sbt-ossuminc itself (the meta-build). The full set
// of plugins RE-EXPORTED to consumers is declared in build.sbt. Keep this
// list minimal. See NOTEBOOK.md "sbt 2.0 Migration Plan" for rationale.
//
// sbt 2.x note: scripted-plugin and dependency-tree are now built into sbt
// core, so no explicit dependency/addDependencyTreePlugin is required.

addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.1.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")

// Resolve a transitive scala-xml version conflict in the meta-build.
ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
ThisBuild / excludeDependencies += "org.scala-lang.modules" % "scala-xml_2.13"
