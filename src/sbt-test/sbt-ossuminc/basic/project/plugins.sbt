// No local exclusion here on purpose: the sbt-ossuminc POM should shield this
// consumer from the scala-xml/scala-collection-compat _2.13 cross-version clash.
addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % sys.props("plugin.version"))
