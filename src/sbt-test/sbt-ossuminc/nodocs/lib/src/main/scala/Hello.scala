/** A documented class that would normally produce Scaladoc output.
  *
  * With.NoDocs should keep this source out of the `doc` task's input set.
  */
class Hello:
  /** Greets the given name. */
  def greet(name: String): String = s"Hello, $name"
