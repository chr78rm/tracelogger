/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.file.FileTracer;
import de.christofreichardt.diagnosis.file.FileTracerLog4jTee;
import de.christofreichardt.diagnosis.io.NullPrintStream;
import de.christofreichardt.diagnosis.io.TracePrintStream;
import de.christofreichardt.diagnosis.net.NetTracer;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Developer
 */
public class LoadUnit {
  
  public static final String PATH_TO_LOGDIR = "." + File.separator + "log" + File.separator + "load";
  final private BannerPrinter bannerPrinter = new BannerPrinter();
  
  @Before
  public void setup() throws IOException {
    File logDir = new File(PATH_TO_LOGDIR);
    File[] logFiles = logDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathName) {
        return pathName.getName().endsWith("log");
      }
    });
    for (File logFile : logFiles) {
      System.out.printf("Deleting '%s'.%n", logFile.getName());
      if (!logFile.delete()) throw new IOException("Cannot delete '" + logFile.getAbsolutePath() + "'.");
    }
  }
  
  @After
  public void tearDown() {
    LogManager.shutdown();
  }
  
  @Test
  public void fileTracer() throws InterruptedException, BrokenBarrierException, IOException {
    this.bannerPrinter.start("fileTracer", getClass());
    
    FileTracer fileTracer = new FileTracer("Test");
    fileTracer.setLogDirPath(new File(PATH_TO_LOGDIR).toPath());
    runScenario(fileTracer, new TestClass(fileTracer));
    List<String> lines = Files.readAllLines(new File(PATH_TO_LOGDIR + File.separator + "Test.log").toPath(), Charset.defaultCharset());
    int c1 = 0, c2 = 0;
    for (String line : lines) {
      if (line.startsWith("* WARNING *")) {
        if (line.endsWith("\"Eine Exception zu Testzwecken.\""))
          c1++;
        else if (line.endsWith("\"Nur zum testen.\""))
          c2++;
      }
      Assert.assertTrue("No SEVERE messages expected.", !line.startsWith("* SEVERE *"));
    }
    Assert.assertTrue("Expected 300 logged exceptions.", c1 == 300);
    Assert.assertTrue("Expected 3 logged messages.", c2 == 3);
  }
  
  @Test
  public void netTracer() throws InterruptedException, ExecutionException, TimeoutException, BrokenBarrierException {
    this.bannerPrinter.start("netTracer", getClass());
    
    final int TIME_OUT = 10;
    final int PORT_NO = 5010;
    NetTracer netTracer = new NetTracer("Test");
    netTracer.setHostName("localhost");
    netTracer.setPortNo(PORT_NO);
    
    class Receiver implements Callable<Boolean> {
      @Override
      public Boolean call() throws java.lang.Exception {
        try (ServerSocket listener = new ServerSocket(PORT_NO)) {
          try (Socket socket = listener.accept();) {
            int c1 = 0, c2 = 0;
            LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
            String line;
            do {
              if ((line = lineNumberReader.readLine()) != null) {
                if (line.startsWith("* WARNING *")) {
                  if (line.endsWith("\"Eine Exception zu Testzwecken.\""))
                    c1++;
                  else if (line.endsWith("\"Nur zum testen.\""))
                    c2++;
                }
                else if (line.startsWith("* SEVERE *")) {
                  throw new RuntimeException("No SEVERE messages expected.");
                }
              }
            } while (line != null);
            
            if (c1 != 300)
              throw new RuntimeException("Expected 300 logged exceptions.");
            else if (c2 != 3)
              throw new RuntimeException("Expected 3 logged messages.");
          }
        }
        
        return true;
      }
    }
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    try {
      Future<Boolean> future = executorService.submit(new Receiver());
      Thread.sleep(2500);
      runScenario(netTracer, new TestClass(netTracer));
      Assert.assertTrue(future.get(TIME_OUT, TimeUnit.SECONDS));
    }
    finally {
      executorService.shutdown();
    }
  }
  
  @Test
  public void log4jTee() throws InterruptedException, BrokenBarrierException, IOException {
    this.bannerPrinter.start("log4jTee", getClass());
    
    DOMConfigurator.configure("." + File.separator + "config" + File.separator + "log4j_load.xml");
    FileTracerLog4jTee fileTracerLog4jTee = new FileTracerLog4jTee("Test");
    fileTracerLog4jTee.setLogDirPath(new File(PATH_TO_LOGDIR).toPath());
    runScenario(fileTracerLog4jTee, new TestClass(fileTracerLog4jTee));
    List<String> lines = Files.readAllLines(new File(PATH_TO_LOGDIR + File.separator + "Test.log").toPath(), Charset.defaultCharset());
    Assert.assertTrue("Expected 44437 lines.", lines.size() == 44437);
    int c1 = 0, c2 = 0;
    for (String line : lines) {
      if (line.startsWith("* WARNING *")) {
        if (line.endsWith("\"Eine Exception zu Testzwecken.\""))
          c1++;
        else if (line.endsWith("\"Nur zum testen.\""))
          c2++;
      }
      Assert.assertTrue("No SEVERE messages expected.", !line.startsWith("* SEVERE *"));
    }
    Assert.assertTrue("Expected 300 logged exceptions.", c1 == 300);
    Assert.assertTrue("Expected 3 logged messages.", c2 == 3);
  }
  
  @Test
  public void corruptedStack() throws InterruptedException, BrokenBarrierException, IOException {
    this.bannerPrinter.start("corruptedStack", getClass());
    
    FileTracer fileTracer = new FileTracer("Test");
    fileTracer.setLogDirPath(new File(PATH_TO_LOGDIR).toPath());
    TestClass testObject = new TestClass(fileTracer);
    testObject.setEmptyStackCorruption(true);
    runScenario(fileTracer, testObject);
    List<String> lines = Files.readAllLines(new File(PATH_TO_LOGDIR + File.separator + "Test.log").toPath(), Charset.defaultCharset());
    Set<String> enabledThreadNames = new HashSet<>(Arrays.asList("main", "TestThread-0", "TestThread-1"));
    Set<String> disabledThreadNames = new HashSet<>();
    for (String line : lines) {
      if (line.startsWith("* SEVERE *")) {
        boolean hit = false;
        for (String threadName : enabledThreadNames) {
          hit = line.contains(threadName);
          if (hit) {
            enabledThreadNames.remove(threadName);
            disabledThreadNames.add(threadName);
            break;
          }
        }
        Assert.assertTrue("Unexpected thread.", hit);
        Assert.assertTrue("Unexpected message.", line.endsWith("\"Stack is corrupted. Tracing is off.\""));
      }
      else if (line.startsWith("ENTRY--") || line.startsWith("RETURN-")) {
        for (String threadName : disabledThreadNames) {
          Assert.assertTrue("Tracing should be off for thread '" + threadName +"'.", !line.contains(threadName));
        }
      }
    }
    Assert.assertTrue("Expected SEVERE messages within all involved threads.", enabledThreadNames.isEmpty());
    Assert.assertTrue("Expected disabled tracing.", fileTracer.out() instanceof NullPrintStream);
    Assert.assertTrue("Expected three disabled threads.", disabledThreadNames.size() == 3);
  }
  
  @Test
  public void stackOverflow() throws InterruptedException, BrokenBarrierException, IOException {
    this.bannerPrinter.start("stackOverflow", getClass());
    FileTracer fileTracer = new FileTracer("Test");
    fileTracer.setLogDirPath(new File(PATH_TO_LOGDIR).toPath());
    TestClass testObject = new TestClass(fileTracer);
    testObject.setStackOverFlowCorruption(true);
    runScenario(fileTracer, testObject);
    List<String> lines = Files.readAllLines(new File(PATH_TO_LOGDIR + File.separator + "Test.log").toPath(), Charset.defaultCharset());
    Set<String> enabledThreadNames = new HashSet<>(Arrays.asList("main", "TestThread-0", "TestThread-1"));
    Set<String> disabledThreadNames = new HashSet<>();
    for (String line : lines) {
      if (line.startsWith("* SEVERE *")) {
        boolean hit = false;
        for (String threadName : enabledThreadNames) {
          hit = line.contains(threadName);
          if (hit) {
            enabledThreadNames.remove(threadName);
            disabledThreadNames.add(threadName);
            break;
          }
        }
        Assert.assertTrue("Unexpected thread.", hit);
        Assert.assertTrue("Unexpected message.", line.endsWith("\"Stacksize is exceeded. Tracing is off.\""));
      }
      else if (line.startsWith("ENTRY--") || line.startsWith("RETURN-")) {
        for (String threadName : disabledThreadNames) {
          Assert.assertTrue("Tracing should be off for thread '" + threadName +"'.", !line.contains(threadName));
        }
      }
    }
    Assert.assertTrue("Expected SEVERE messages within all involved threads.", enabledThreadNames.isEmpty());
    Assert.assertTrue("Expected disabled tracing.", fileTracer.out() instanceof NullPrintStream);
    Assert.assertTrue("Expected three disabled threads.", disabledThreadNames.size() == 3);
  }

  private void runScenario(AbstractTracer tracer, TestClass testObject) throws InterruptedException, BrokenBarrierException {
    try {
      tracer.open();
      tracer.out().printIndentln("This is a test."); // will be ignored
      tracer.initCurrentTracingContext(5, true);
      Assert.assertTrue("Expected enabled tracing.", tracer.out() instanceof TracePrintStream);
      tracer.entry("void", this, "runScenario(AbstractTracer tracer, TestClass testClass)");

      try {
        tracer.out().printfIndentln("tracer.getName() = %s", tracer.getName());
        tracer.out().printIndentln("This is a test.");

        final int NUMBER_OF_TESTTHREADS = 2;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(NUMBER_OF_TESTTHREADS + 1);
        Thread[] testThreads = new Thread[NUMBER_OF_TESTTHREADS];
        for (int i = 0; i < NUMBER_OF_TESTTHREADS; i++) {
          TestRunnable testRunnable = new TestRunnable(tracer);
          testRunnable.setTestObject(testObject);
          testRunnable.setCyclicBarrier(cyclicBarrier);
          testThreads[i] = new Thread(testRunnable, "TestThread-" + i);
          testThreads[i].start();
        }
        cyclicBarrier.await();
        testObject.performTests();

        for (Thread testThread : testThreads) {
          testThread.join();
        }
      }
      finally {
        tracer.wayout();
      }
    }
    finally {
      tracer.close();
    }
  }

}
