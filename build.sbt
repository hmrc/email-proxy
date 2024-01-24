val appName = "email-proxy"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    scalaVersion := "2.13.12",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings( majorVersion := 1 )
  .settings(ScoverageSettings())
