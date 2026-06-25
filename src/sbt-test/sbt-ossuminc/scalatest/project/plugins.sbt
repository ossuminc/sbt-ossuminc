sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}

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
