package chiseltest.internal

import chisel3.{Data, Module}
import chiseltest.coverage.TestCoverage
import chiseltest.simulator.SimulatorContext
import firrtl.AnnotationSeq

class SingleThreadBackend[T <: Module](
  val dut:             T,
  val dataNames:       Map[Data, String],
  val tester:          SimulatorContext,
  coverageAnnotations: AnnotationSeq)
    extends GenericBackend[T]
    with BackendInstance[T] {
  def run(testFn: T => Unit): AnnotationSeq = {
    try {
      // default reset
      pokeBits(dut.reset, 1)
      tester.step(1)
      pokeBits(dut.reset, 0)

      // execute use code
      testFn(dut)
    } finally {
      tester.finish() // needed to dump VCDs + terminate any external process
    }

    if (tester.sim.supportsCoverage) {
      TestCoverage(tester.getCoverage()) +: coverageAnnotations
    } else { Seq() }
  }
}
