import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val bootstrapVersion = "9.16.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.scalatest"          %% "scalatest"              % "3.2.17" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"     % "7.0.1" % Test,
    "org.scalatestplus"      %% "mockito-4-11"           % "3.2.18.0" % Test
  )
}
