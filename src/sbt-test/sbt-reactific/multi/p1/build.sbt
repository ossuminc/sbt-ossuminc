name := "project1"

unmanagedResourceDirectories in compile := Seq(
  baseDirectory.value / "src/resources"
)
