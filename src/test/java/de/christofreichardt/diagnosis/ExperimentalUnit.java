/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.net.NetTracer;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Developer
 */
public class ExperimentalUnit {
  final private BannerPrinter bannerPrinter = new BannerPrinter();

  @Test
  @Ignore
  public void experiment() {
    this.bannerPrinter.start("experiment", getClass());
    
    final int PORT_NO = 5010;
    NetTracer tracer = new NetTracer("Test");
    tracer.setHostName("localhost");
    tracer.setPortNo(PORT_NO);
    tracer.open();
    tracer.initCurrentTracingContext(5, true);
    TestClass testObject = new TestClass(tracer);
    testObject.performTests();
//    tracer.out().printfIndentln("This is a test.");
  }
}
