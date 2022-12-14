import sbt.Tests.{Group, SubProcess}
import sbt._

object TestPhases {
  def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
    tests map { test =>
      val forkOptions = ForkOptions()
        .withOutputStrategy(OutputStrategy.StdoutOutput)
        .withRunJVMOptions(Vector("-Dtest.name=" + test.name))

      Group(test.name, Seq(test), SubProcess(forkOptions))
    }
}
