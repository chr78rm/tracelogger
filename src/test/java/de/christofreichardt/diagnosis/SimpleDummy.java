/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.io.NullPrintStream;
import de.christofreichardt.diagnosis.io.TracePrintStream;
import org.junit.Assert;

/**
 *
 * @author Developer
 */
public class SimpleDummy {
  final AbstractTracer tracer;

  public SimpleDummy(AbstractTracer tracer) {
    this.tracer = tracer;
  }
  
  public void method_0() {
    Assert.assertTrue("Expected a NullPrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof NullPrintStream);
    tracer.entry("void", this, "method_0()");
    try {
      Assert.assertTrue("Expected a NullPrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof NullPrintStream);
    }
    finally {
      tracer.wayout();
      Assert.assertTrue("Expected a NullPrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof NullPrintStream);
    }
  }

  public void method_1() {
    Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
    tracer.entry("void", this, "method_1()");
    try {
      Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
      method_2();
    }
    finally {
      tracer.wayout();
      Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
    }
  }

  void method_2() {
    Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
    tracer.entry("void", this, "method_2()");
    try {
      Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
      method_3();
    }
    finally {
      tracer.wayout();
      Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
    }
  }

  void method_3() {
    Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
    tracer.entry("void", this, "method_3()");
    try {
      Assert.assertTrue("Expected a NullPrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof NullPrintStream);
      tracer.logMessage(LogLevel.INFO, "This is a test.", getClass(), "method_3()");
      tracer.out().printfIndentln("This output goes to /dev/null.");
    }
    finally {
      tracer.wayout();
      Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
    }
  }
}
