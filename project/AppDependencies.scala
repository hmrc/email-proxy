import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val bootstrapVersion = "8.4.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"   % bootstrapVersion % Test,
    "org.scalatest"          %% "scalatest"                % "3.2.17" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0" % Test,
    "com.vladsch.flexmark"   % "flexmark-all"              % "0.36.8" % Test,
    "org.mockito"            % "mockito-core"              % "5.9.0" % Test
  )
}