import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "5.7.0"
  )

  def test(scope: String = "test"): Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"                % "3.2.9" % scope,
    "org.pegdown"            % "pegdown"                   % "1.6.0" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0" % scope,
    "com.vladsch.flexmark"   % "flexmark-all"              % "0.36.8" % scope,
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.9.0",
    "org.mockito"            % "mockito-core"              % "3.11.2"
  )
}