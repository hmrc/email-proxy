import TestPhases.oneForkedJvmPerTest
import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption

val appName = "email-proxy"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    scalaVersion := "2.13.8",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test(),
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest                  := false,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest) (base => Seq(base / "it")).value,
    testGrouping in IntegrationTest               := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest          := false,
    addTestReportOption(IntegrationTest, "int-test-reports")
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings( majorVersion := 1 )
  .settings(ScoverageSettings())
