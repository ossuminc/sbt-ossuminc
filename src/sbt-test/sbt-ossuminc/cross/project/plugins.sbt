// Uncomment this and set it to the locally published sbt-riddl version you want
// to test manually and then run sbt from the command line in src/sbt-test/sbt-ossuminc/native
// This subverts the use of plubing.version system property which has the value
// derived from the plugin's sbt settings
// addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "0.7.1-0-7c3df014-20240116-2229")

addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % sys.props("plugin.version"))
