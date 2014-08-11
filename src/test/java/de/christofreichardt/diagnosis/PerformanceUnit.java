/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.diagnosis;

import de.christofreichardt.junit.Measure;
import java.math.BigInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Developer
 */
public class PerformanceUnit {
  
  final private BannerPrinter bannerPrinter = new BannerPrinter();
  
  @Rule
  public Measure measure;

  public PerformanceUnit() {
    this.measure = new Measure(1, 3);
  }
  
  @Before
  public void setUp() {

  }
  
  @After
  public void tearDown() {
  }
  
  @Test
  @Ignore
  public void defaultTracer() {
    this.bannerPrinter.start("defaultTracer", getClass());
    
    TracerFactory.getInstance().reset();
    NullTracer tracer = TracerFactory.getInstance().getDefaultTracer();
    BigInteger sum = new BigInteger("0");
    for (long i=0; i<1000*1000*1000; i++) {
      sum = sum.add(BigInteger.valueOf(i));
      tracer.out().printfIndentln("sum = %d", sum);
    }
    System.out.printf("sum = %d%n", sum);
  }
}
