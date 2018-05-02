/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.BannerPrinter;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.QueueNullTracer;
import de.christofreichardt.diagnosis.QueueTracer;
import de.christofreichardt.diagnosis.SimpleDummy;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.diagnosis.io.NullPrintStream;
import de.christofreichardt.diagnosis.io.TracePrintStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Developer
 */
public class QueueFileTracerUnit {
  public static final File logDir = new File("." + File.separator + "log" + File.separator + "queue");
  final private BannerPrinter bannerPrinter = new BannerPrinter();
  
//  class SimpleDummy {
//
//    final AbstractTracer tracer;
//
//    public SimpleDummy(AbstractTracer tracer) {
//      this.tracer = tracer;
//    }
//
//    public void method_0() {
//      Assert.assertTrue("Expected a NullPrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof NullPrintStream);
//      tracer.entry("void", this, "method_0()");
//      try {
//        Assert.assertTrue("Expected a NullPrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof NullPrintStream);
//      }
//      finally {
//        tracer.wayout();
//        Assert.assertTrue("Expected a NullPrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof NullPrintStream);
//      }
//    }
//
//    public void method_1() {
//      Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
//      tracer.entry("void", this, "method_1()");
//      try {
//        Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
//        method_2();
//      }
//      finally {
//        tracer.wayout();
//        Assert.assertTrue("Expected a NullPrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof NullPrintStream);
//      }
//    }
//
//    void method_2() {
//      Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
//      tracer.entry("void", this, "method_2()");
//      try {
//        Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
//        tracer.out().printfIndentln("This is a test.");
//        method_3();
//      }
//      finally {
//        tracer.wayout();
//        Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
//      }
//    }
//
//    void method_3() {
//      Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
//      tracer.entry("void", this, "method_3()");
//      try {
//        Assert.assertTrue("Expected a NullPrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof NullPrintStream);
//        tracer.out().printfIndentln("This output goes to /dev/null.");
//      }
//      finally {
//        tracer.wayout();
//        Assert.assertTrue("Expected a TracePrintStream but found a " + tracer.out().getClass().getSimpleName(), tracer.out() instanceof TracePrintStream);
//      }
//    }
//  }

  
  @Before
  public void setup() throws IOException {
    File[] logFiles = QueueFileTracerUnit.logDir.listFiles((File file) -> file.getName().endsWith("log") && !"empty.log".equals(file.getName()));
    for (File logFile : logFiles) {
      System.out.printf("Deleting '%s'.%n", logFile.getName());
      if (!logFile.delete()) throw new IOException("Cannot delete '" + logFile.getAbsolutePath() + "'.");
    }
  }
  
  @Test
  public void simpleUse() throws InterruptedException, TracerFactory.Exception, FileNotFoundException, IOException {
    this.bannerPrinter.start("simpleUse", getClass());
    
    class Consumer implements Callable<Boolean> {
      final int id;

      public Consumer(int i) {
        this.id = i;
      }
      
      @Override
      public Boolean call() throws java.lang.Exception {
        AbstractTracer tracer = TracerFactory.getInstance().takeTracer();
        Assert.assertTrue("Expected the previously took tracer.", TracerFactory.getInstance().getCurrentQueueTracer().hashCode() == tracer.hashCode());
        SimpleDummy simpleDummy = new SimpleDummy(tracer);
        simpleDummy.method_0();
        tracer.initCurrentTracingContext();
        tracer.entry("Boolean", this, "call()");
        try {
          tracer.logMessage(LogLevel.INFO, "Consumer(" + this.id + ")", getClass(), "call()");
          simpleDummy.method_1();
//          if (this.id == 67)
//            return false;
          return true;
        }
        finally {
          tracer.wayout();
          Assert.assertTrue("Expected the QueueNullTracer.", TracerFactory.getInstance().getCurrentQueueTracer() instanceof QueueNullTracer);
        }
      }
    }
    
    TracerFactory.getInstance().reset();
    File configFile = new File("." + File.separator + "config" + File.separator + "TraceConfig.xml");
    TracerFactory.getInstance().readConfiguration(configFile);
    Assert.assertTrue("Problems when opening the queue tracer.", TracerFactory.getInstance().openQueueTracer());
    final int THREAD_NUMBER = 10;
    final int ITERATIONS = 100;
    final int TIMEOUT = 5;
    List<Future<Boolean>> futures = new ArrayList<>();
    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUMBER);
    try {
      for (int i = 0; i < ITERATIONS; i++) {
        futures.add(executorService.submit(new Consumer(i)));
      }
      try {
        for (Future<Boolean> future : futures) {
          Assert.assertTrue("Expected a true result.", future.get());
        }
      }
      catch (ExecutionException ex) {
        ex.getCause().printStackTrace(System.err);
        Assert.fail(ex.getMessage());
      }
    }
    finally {
      executorService.shutdown();
      Assert.assertTrue("Threadpool hasn't terminated yet.", executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS));
    }
    
    Assert.assertTrue("Problems when closing the queue tracer.", TracerFactory.getInstance().closeQueueTracer());
    File[] logFiles = QueueFileTracerUnit.logDir.listFiles((File file) -> file.getName().endsWith("log") && !"empty.log".equals(file.getName()));
    Assert.assertTrue("Expected " + TracerFactory.getInstance().getQueueSize() + " logfiles.", logFiles.length == TracerFactory.getInstance().getQueueSize());
    Set<String> expectedConsumers = new HashSet<>();
    for (int i=0; i<ITERATIONS; i++) {
      expectedConsumers.add("Consumer(" + i + ")");
    }
    Pattern pattern = Pattern.compile("Consumer\\([0-9]+\\)");
    for (File logFile : logFiles) {
      List<String> lines = Files.readAllLines(logFile.toPath(), Charset.defaultCharset());
      for (String line : lines) {
        if (line.startsWith("* INFO *") && !line.endsWith("\"This is a test.\"")) {
          Matcher matcher = pattern.matcher(line);
          Assert.assertTrue(matcher.find());
          Assert.assertTrue(expectedConsumers.remove(line.substring(matcher.start(), matcher.end())));
        }
      }
    }
    Assert.assertTrue("Consumer missing.", expectedConsumers.isEmpty());
  }
}
