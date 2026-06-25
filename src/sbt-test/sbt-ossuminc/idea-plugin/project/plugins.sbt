addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % sys.props("plugin.version"))

// sbt 2 transition: shield the test meta-build from a transitive
// scala-xml/scala-collection-compat _2.13 vs _3 cross-version clash.
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
  "org.scala-lang.modules" %% "scala-collection-compat" % VersionScheme.Always
)
ThisBuild / excludeDependencies ++= Seq(
  "org.scala-lang.modules" % "scala-xml_2.13",
  "org.scala-lang.modules" % "scala-collection-compat_2.13"
)
