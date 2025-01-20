import scala.sys.process.{Process, ProcessLogger}

import com.ossuminc.sbt.OssumIncPlugin.autoImport.Plugin

enablePlugins(OssumIncPlugin)

lazy val root = Root("multi")
  .aggregate(p1, p2)
  .configure(With.noPublishing)
  .settings(
    TaskKey[Unit]("lslrt") := {
      val proc = Process(Seq("ls", "-lrt"))
      proc.run(handyPL)
      ()
    }
  )

lazy val handyPL = new ProcessLogger {
  def out(s: => String): Unit = println(s)

  def err(s: => String): Unit = println(s)

  def buffer[T](f: => T): T = f
}

lazy val p1 = Module("project1", "p1")
  .configure(With.typical)
  .settings(
    Compile / unmanagedResourceDirectories := Seq(
      baseDirectory.value / "src" / "resources"
    )
  )

lazy val p2 = Module("project2", "p2")
  .configure(With.typical)
  .dependsOn(p1)

lazy val plugin = Plugin("plugin", "plugin")
  .settings(
    maxErrors := 50,
    TaskKey[Unit]("check") := {
      println(s"Checking from within sbt:")
      if (sbtPlugin.value == true)
        println("Confirmed as plugin")
      else
        throw new Exception("Failed to be a plugin")
    }
  )
