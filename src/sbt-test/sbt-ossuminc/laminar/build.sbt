enablePlugins(OssumIncPlugin)

lazy val root = Module("laminar-test", "laminar-test")
  .configure(
    With.basic,
    With.Javascript(),
    With.Laminar()
  )
  .settings(
    scalaJSUseMainModuleInitializer := true
  )
