import scala.scalajs.js.annotation.JSExportTopLevel

/** Shared cross-platform code that uses a Scala.js export annotation.
  *
  * The `@JSExportTopLevel` symbol comes from `scala.scalajs.js.annotation`,
  * which is only on the classpath of the JS row unless the JVM and Native rows
  * also get `scalajs-stubs`. CrossModule must auto-add those stubs, so this
  * file compiles on all three rows with NO consumer-side dependency.
  */
@JSExportTopLevel("Greeter")
object Greeter:
  def greet(name: String): String = s"Hello, $name"
