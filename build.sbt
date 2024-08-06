val appName = "email-proxy"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    scalaVersion := "3.4.2",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(majorVersion := 2)
  .settings(ScoverageSettings())

Test / test := (Test / test)
  .dependsOn(scalafmtCheckAll)
  .value