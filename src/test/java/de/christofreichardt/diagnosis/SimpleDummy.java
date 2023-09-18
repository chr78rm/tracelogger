/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.io.NullPrintStream;
import de.christofreichardt.diagnosis.io.TracePrintStream;
import org.assertj.core.api.WithAssertions;

/**
 *
 * @author Developer
 */
public class SimpleDummy implements WithAssertions {
  final AbstractTracer tracer;

  public SimpleDummy(AbstractTracer tracer) {
    this.tracer = tracer;
  }
  
  public void method_0() {
    assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
    tracer.entry("void", this, "method_0()");
    try {
      assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
    }
    finally {
      tracer.wayout();
      assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
    }
  }

  public void method_1() {
    assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
    tracer.entry("void", this, "method_1()");
    try {
      assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
      method_2();
    }
    finally {
      tracer.wayout();
      assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
    }
  }

  void method_2() {
    assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
    tracer.entry("void", this, "method_2()");
    try {
      assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
      method_3();
    }
    finally {
      tracer.wayout();
      assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
    }
  }

  void method_3() {
    assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
    tracer.entry("void", this, "method_3()");
    try {
      assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
      tracer.logMessage(LogLevel.INFO, "This is a test.", getClass(), "method_3()");
      tracer.out().printfIndentln("This output goes to /dev/null.");
    }
    finally {
      tracer.wayout();
      assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
    }
  }
}
