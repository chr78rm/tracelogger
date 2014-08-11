/*
 * TestThread.java
 */
package de.christofreichardt.diagnosis;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * This Runnable will be used within several test cases to demonstrate the tracing behaviour when multiple threads are using the same tracer.
 *
 * @author Christof Reichardt
 */
public class TestRunnable implements Runnable {

  private AbstractTracer tracer = null;
  private CyclicBarrier cyclicBarrier = null;
  private TestClass testObject = null;

  /**
   * Creates a new instance of TestRunnable
   *
   * @param tracer the tracer to test
   */
  public TestRunnable(AbstractTracer tracer) {
    this.tracer = tracer;
  }

  /**
   * Calls a test class which will perform some tests.
   */
  @Override
  public void run() {
    this.tracer.initCurrentTracingContext(5, true);
    this.tracer.entry("void", this, "run()");

    try {
      try {
        this.cyclicBarrier.await();
      }
      catch (InterruptedException | BrokenBarrierException ex) {
        ex.printStackTrace(System.err);
      }

      if (this.testObject == null) {
        this.testObject = new TestClass(this.tracer);
      }

      this.testObject.performTests();
    }
    finally {
      this.tracer.wayout();
    }
  }

  /**
   * cyclicBarrier setter.
   *
   * @param cyclicBarrier a synchronization aid
   */
  public void setCyclicBarrier(CyclicBarrier cyclicBarrier) {
    this.cyclicBarrier = cyclicBarrier;
  }

  /**
   * testClass setter.
   *
   * @param testClass a test instance
   */
  public void setTestObject(TestClass testObject) {
    this.testObject = testObject;
  }

}
