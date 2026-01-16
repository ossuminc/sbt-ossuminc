enablePlugins(OssumIncPlugin)

lazy val root = Root("laminar-test", startYr = 2024)
  .configure(With.noPublishing)
  .aggregate(app)

lazy val app = Module("app", "laminar-app")
  .configure(
    With.typical,
    With.ScalaJS(),
    With.Laminar()
  )
  .settings(
    scalaJSUseMainModuleInitializer := true
  )
