/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.file.FileTracer;
import de.christofreichardt.diagnosis.file.FileTracerLog4jTee;
import de.christofreichardt.diagnosis.net.NetTracer;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

/**
 *
 * @author Developer
 */
public class TracerFactoryUnit {
  public static final String PATH_TO_LOGDIR = "." + File.separator + "log";

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  final private BannerPrinter bannerPrinter = new BannerPrinter();
  
  public TracerFactoryUnit() {
  }
  
  @Before
  public void setUp() throws IOException {
    TracerFactory.getInstance().reset();
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
  }

  /**
   * Test of getInstance method, of class TracerFactory.
   */
  @Test
  public void getInstance() {
    this.bannerPrinter.start("getInstance", getClass());
    
    TracerFactory instance = TracerFactory.getInstance();
    assertNotNull("Expected a TracerFactory instance.", instance);
  }

  /**
   * Test of getDefaultTracer method, of class TracerFactory.
   */
  @Test
  public void getDefaultTracer() {
    this.bannerPrinter.start("getDefaultTracer", getClass());
    
    AbstractTracer defaultTracer = TracerFactory.getInstance().getDefaultTracer();
    assertNotNull("Expected a Tracer instance.", defaultTracer);
    assertTrue("Expected a NullTracer instance.", defaultTracer instanceof NullTracer);
    assertTrue("Expected the \"TracerFactory.NULLTRACER\".", System.identityHashCode(defaultTracer) == System.identityHashCode(TracerFactory.NULLTRACER));
  }

  /**
   * Test of readConfiguration method, of class TracerFactory.
   * @throws java.lang.InterruptedException
   * @throws java.util.concurrent.ExecutionException
   */
  @Test
  public void readConfiguration_File() throws InterruptedException, ExecutionException {
    this.bannerPrinter.start("readConfiguration_File", getClass());
    
    File configFile = new File("." + File.separator + "config" + File.separator + "TraceConfig.xml");
    try {
      TracerFactory.getInstance().readConfiguration(configFile);
      
      Class<?>[] tracerClasses = {FileTracer.class, FileTracer.class, NetTracer.class, NullTracer.class, FileTracerLog4jTee.class, FileTracer.class};
      for (int i=0; i<tracerClasses.length; i++) {
        String tracerName = "TestTracer-" + i;
        AbstractTracer testTracer = TracerFactory.getInstance().getTracer(tracerName);
        assertNotNull("Expected a Tracer instance.", testTracer);
        assertTrue("Unexpected tracer name.", tracerName.equals(testTracer.getName()));
        assertTrue("Unexpected tracer class.", testTracer.getClass().equals(tracerClasses[i]));
      }
      
      AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
      assertTrue("Expected the TestTracer-0", "TestTracer-0".equals(tracer.getName()));
      for (int i=0; i<=2; i++) {
        final String THREAD_NAME = "TestThread-" + i;
        final String TRACER_NAME = "TestTracer-" + i;
        ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
          @Override
          public Thread newThread(Runnable runnable) {
            return new Thread(runnable, THREAD_NAME);
          }
        });
        try {
          Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws java.lang.Exception {
              AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
              return TRACER_NAME.equals(tracer.getName());
            }
          });
          assertTrue("Tracermappings failed.", future.get());
        }
        finally {
          executorService.shutdown();
        }
      }
      ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
          return new Thread(runnable, "TestThread-0");
        }
      });
      try {
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
          @Override
          public Boolean call() throws java.lang.Exception {
            AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
            AbstractTracer defaultTracer = TracerFactory.getInstance().getDefaultTracer();
            return System.identityHashCode(defaultTracer) == System.identityHashCode(tracer);
          }
        });
        assertTrue("Safety check failed.", future.get());
      }
      finally {
        executorService.shutdown();
      }
      
      assertTrue("Queue should be enabled.", TracerFactory.getInstance().isQueueEnabled());
      assertTrue("Size of the queue is expected to be five.", TracerFactory.getInstance().getQueueSize() == 5);
      assertTrue("Wrong QueueTracer class.", "de.christofreichardt.diagnosis.file.QueueFileTracer".equals(TracerFactory.getInstance().getQueueTracerClassname()));
    }
    catch (TracerFactory.Exception | FileNotFoundException ex) {
      fail("'" + configFile.getAbsolutePath() + "' should be accepted.");
    }
  }

  /**
   * Test of getTracer method, of class TracerFactory.
   * @throws de.christofreichardt.diagnosis.TracerFactory.Exception
   */
  @Test
  public void unknownTracerByName() throws TracerFactory.Exception {
    this.bannerPrinter.start("unknownTracerByName", getClass());
    
    this.thrown.expect(TracerFactory.Exception.class);
    this.thrown.expectMessage("Unknown tracer:");
    TracerFactory.getInstance().getTracer("TestTracer-0");
  }

  /**
   * Test of getTracer method, of class TracerFactory.
   */
  @Test
  public void unkownTracerByMapping() {
    this.bannerPrinter.start("unkownTracerByMapping", getClass());
    
    AbstractTracer currentTracer = TracerFactory.getInstance().getCurrentPoolTracer();
    AbstractTracer defaultTracer = TracerFactory.getInstance().getDefaultTracer();
    assertTrue("Expected the default tracer.", System.identityHashCode(defaultTracer) == System.identityHashCode(currentTracer));
  }
  
  /**
   * The InvalidTraceConfig_1.xml configures a tracer class which isn't derived from AbstractTracer
   * @throws TracerFactory.Exception
   */
  @Test
  public void invalidTracerClassConfigured_1() throws TracerFactory.Exception {
    this.bannerPrinter.start("invalidTracerClassConfigured_1", getClass());
    
    this.thrown.expect(TracerFactory.Exception.class);
    this.thrown.expectMessage("Illegal tracer class!");
    File configFile = new File("." + File.separator + "config" + File.separator + "InvalidTraceConfig_1.xml");
    try {
      TracerFactory.getInstance().readConfiguration(configFile);
    }
    catch (FileNotFoundException ex) {
      fail("\"" + configFile.getAbsolutePath() + "\" should be found.");
    }
  }
  
  /**
   * The InvalidTraceConfig_2.xml configures a default tracer class which isn't derived from NullTracer
   * @throws TracerFactory.Exception
   */
  @Test
  public void invalidTracerClassConfigured_2() throws TracerFactory.Exception {
    this.bannerPrinter.start("invalidTracerClassConfigured_2", getClass());
    
    this.thrown.expect(TracerFactory.Exception.class);
    this.thrown.expectMessage("Requiring a NullTracer as default tracer!");
    File configFile = new File("." + File.separator + "config" + File.separator + "InvalidTraceConfig_2.xml");
    try {
      TracerFactory.getInstance().readConfiguration(configFile);
    }
    catch (FileNotFoundException ex) {
      fail("\"" + configFile.getAbsolutePath() + "\" should be found.");
    }
  }
  
  /**
   * The InvalidTraceConfig_3.xml violates the schema
   * @throws TracerFactory.Exception
   */
  @Test
  public void invalidTracerClassConfigured_3() throws TracerFactory.Exception {
    this.bannerPrinter.start("invalidTracerClassConfigured_3", getClass());
    
    this.thrown.expect(TracerFactory.Exception.class);
    this.thrown.expectCause(CoreMatchers.any(SAXException.class));
    File configFile = new File("." + File.separator + "config" + File.separator + "InvalidTraceConfig_3.xml");
    try {
      TracerFactory.getInstance().readConfiguration(configFile);
    }
    catch (FileNotFoundException ex) {
      fail("\"" + configFile.getAbsolutePath() + "\" should be found.");
    }
  }
  
  /**
   * Opens and closes all configured tracers.
   * 
   * @throws de.christofreichardt.diagnosis.TracerFactory.Exception
   * @throws java.io.FileNotFoundException
   * @throws java.lang.InterruptedException
   * @throws java.util.concurrent.ExecutionException
   * @throws java.util.concurrent.TimeoutException
   */
  @Test
  public void openAllAndCloseAll() throws TracerFactory.Exception, FileNotFoundException, InterruptedException, ExecutionException, TimeoutException {
    this.bannerPrinter.start("openAllAndCloseAll", getClass());
    
    File configFile = new File("." + File.separator + "config" + File.separator + "TraceConfig.xml");
    TracerFactory.getInstance().readConfiguration(configFile);
    final int PORT_NO = ((NetTracer) TracerFactory.getInstance().getTracer("TestTracer-2")).getPortNo();
    final int TIME_OUT = 10;
    
    class Receiver implements Callable<Boolean> {
      @Override
      public Boolean call() throws java.lang.Exception {
        try (ServerSocket listener = new ServerSocket(PORT_NO)) {
          try (Socket socket = listener.accept();) {
            LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
            String line;
            do {
              line = lineNumberReader.readLine();
            } while (line != null);
          }
        }
        
        return true;
      }
    }
    
    String[] tracerNames = {"TestTracer-0", "TestTracer-1", "TestTracer-2", "TestTracer-3", "TestTracer-4", "TestTracer-5"};
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    try {
      Future<Boolean> future = executorService.submit(new Receiver());
      try {
        TracerFactory.getInstance().openPoolTracer();
        for (String tracerName : tracerNames) {
          Assert.assertTrue("Tracer '" + tracerName + "' isn't open.", TracerFactory.getInstance().getTracer(tracerName).isOpened());
        }
      }
      finally {
        TracerFactory.getInstance().closePoolTracer();
      }
      Assert.assertTrue("Problems with the configured NetTracer.", future.get(TIME_OUT, TimeUnit.SECONDS));
      Assert.assertTrue("TestTracer-0.log missing.", new File(PATH_TO_LOGDIR + File.separator + "TestTracer-0.log").exists());
      Assert.assertTrue("TestTracer-1.log missing.", new File(PATH_TO_LOGDIR + File.separator + "TestTracer-1.log").exists());
      Assert.assertTrue("NullTracer must not produce a log.", !new File(PATH_TO_LOGDIR + File.separator + "TestTracer-3.log").exists());
      Assert.assertTrue("TestTracer-4.log missing.", new File(PATH_TO_LOGDIR + File.separator + "TestTracer-4.log").exists());
      Assert.assertTrue("TestTracer-5.log missing.", new File(PATH_TO_LOGDIR + File.separator + "TestTracer-5.log").exists());
    }
    finally {
      executorService.shutdown();
    }
  }
  
  /**
   * Tests output redirection with a configured tracing context.
   */
  @Test
  public void debugLevel() {
    this.bannerPrinter.start("debugLevel", getClass());
    
    File configFile = new File("." + File.separator + "config" + File.separator + "TraceConfig.xml");
    try {
      TracerFactory.getInstance().readConfiguration(configFile);
      final AbstractTracer tracer = TracerFactory.getInstance().getTracer("TestTracer-5");
      tracer.open();
      try {
        SimpleDummy simpleDummy = new SimpleDummy(tracer);
        simpleDummy.method_0();
        tracer.initCurrentTracingContext();
        simpleDummy.method_1();
      }
      finally {
        tracer.close();
      }
    }
    catch (TracerFactory.Exception | FileNotFoundException ex) {
      fail("'" + configFile.getAbsolutePath() + "' should be accepted.");
    }
  }
  
  /**
   * Tests if the tracer queue is effectively disabled by the <Disabled/>-tag.
   * @throws java.io.FileNotFoundException
   * @throws de.christofreichardt.diagnosis.TracerFactory.Exception
   */
  @Test
  public void disabledQueueTraceConfig_1() throws FileNotFoundException, TracerFactory.Exception {
    this.bannerPrinter.start("disabledQueueTraceConfig_1", getClass());
    
    File configFile = new File("." + File.separator + "config" + File.separator + "DisabledQueueTraceConfig_1.xml");
    TracerFactory.getInstance().readConfiguration(configFile);
    assertQueueIsDisabled();
  }
  
  /**
   * Tests if the tracer queue is effectively disables by a missing <Queue/>-tag.
   * @throws java.io.FileNotFoundException
   * @throws de.christofreichardt.diagnosis.TracerFactory.Exception
   */
  @Test
  public void disabledQueueTraceConfig_2()  throws FileNotFoundException, TracerFactory.Exception {
    this.bannerPrinter.start("disabledQueueTraceConfig_2", getClass());
    
    File configFile = new File("." + File.separator + "config" + File.separator + "DisabledQueueTraceConfig_2.xml");
    TracerFactory.getInstance().readConfiguration(configFile);
    assertQueueIsDisabled();
  }
  
  private void assertQueueIsDisabled() {
    Assert.assertTrue("Expected zero queue size.", TracerFactory.getInstance().getQueueSize() == 0);
    Assert.assertTrue("Expected disabled queue.", !TracerFactory.getInstance().isQueueEnabled());
    Assert.assertTrue("Expected a NullTracer.", TracerFactory.getInstance().takeTracer() instanceof QueueNullTracer);
    
    class Consumer implements Callable<Boolean> {
      @Override
      public Boolean call() throws java.lang.Exception {
        final int ITERATIONS = 100 + TracerFactory.getInstance().getQueueSize();
        for (int i=0; i<ITERATIONS; i++) {
          TracerFactory.getInstance().takeTracer();
        }
        return true;
      }
    }
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Future<Boolean> future = executorService.submit(new Consumer());
    try {
      future.get(10, TimeUnit.SECONDS);
    }
    catch (InterruptedException | ExecutionException | TimeoutException ex) {
      fail("Expected no problems by executing the Consumer.");
    }
  }
}
