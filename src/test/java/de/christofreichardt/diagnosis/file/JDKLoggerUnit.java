/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.BannerPrinter;
import de.christofreichardt.diagnosis.JDKLoggingRouter;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.NullTracer;
import de.christofreichardt.diagnosis.io.NullPrintStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Developer
 */
public class JDKLoggerUnit {
  
  public static final String PATH_TO_LOGDIR = "." + File.separator + "log" + File.separator + "jdk14";
  public static final String PATH_TO_LOGFILE = PATH_TO_LOGDIR + File.separator + "jdk14-test.0.log";
  final private BannerPrinter bannerPrinter = new BannerPrinter();
  
  @Before
  public void setup() throws IOException {
    File logDir = new File(PATH_TO_LOGDIR);
    File[] logFiles = logDir.listFiles((File file) -> file.getName().endsWith("log") && !"empty.log".equals(file.getName()));
    for (File logFile : logFiles) {
      System.out.printf("Deleting '%s'.%n", logFile.getName());
      if (!logFile.delete()) throw new IOException("Cannot delete '" + logFile.getAbsolutePath() + "'.");
    }
  }
  
  @After
  public void tearDown() {
    LogManager.getLogManager().reset();
  }
  
  @Test
  public void jdk14Config() throws IOException {
    this.bannerPrinter.start("jdk14Config", getClass());
    
    File configFile = new File("." + File.separator + "config" + File.separator + "logging.properties");
    try (FileInputStream inputStream = new FileInputStream(configFile)) {
      LogManager.getLogManager().readConfiguration(inputStream);
    }
    Logger.getLogger(getClass().getName()).info("This is a test.");
    Assert.assertTrue(new File(PATH_TO_LOGFILE).exists());
  }
  
  @Test
  public void loggingRouter() throws IOException {
    this.bannerPrinter.start("loggingRouter", getClass());
    
    File configFile = new File("." + File.separator + "config" + File.separator + "logging.properties");
    try (FileInputStream inputStream = new FileInputStream(configFile)) {
      LogManager.getLogManager().readConfiguration(inputStream);
    }
    
    class Dummy {
      final NullTracer tracer;

      public Dummy(NullTracer tracer) {
        this.tracer = tracer;
      }
      
      void method_0() {
        Assert.assertTrue(tracer.out() instanceof NullPrintStream);
        tracer.entry("void", this, "method_0()");
        try {
        }
        finally {
          tracer.wayout();
          Assert.assertTrue(tracer.out() instanceof NullPrintStream);
        }
      }
      void method_1() {
        Assert.assertTrue(tracer.out() instanceof NullPrintStream);
        tracer.entry("void", this, "method_1()");
        try {
          Assert.assertTrue(tracer.out() instanceof NullPrintStream);
          tracer.logMessage(LogLevel.INFO, "Within method_1.", getClass(), "method_1()");
          method_2();
        }
        finally {
          tracer.wayout();
          Assert.assertTrue(tracer.out() instanceof NullPrintStream);
        }
      }
      void method_2() {
        Assert.assertTrue(tracer.out() instanceof NullPrintStream);
        tracer.entry("void", this, "method_2()");
        try {
          Assert.assertTrue(tracer.out() instanceof NullPrintStream);
          tracer.logMessage(LogLevel.INFO, "Within method_2.", getClass(), "method_1()");
          method_3();
        }
        finally {
          tracer.wayout();
          Assert.assertTrue(tracer.out() instanceof NullPrintStream);
        }
      }
      void method_3() {
        Assert.assertTrue(tracer.out() instanceof NullPrintStream);
        tracer.entry("void", this, "method_3()");
        try {
          Assert.assertTrue(tracer.out() instanceof NullPrintStream);
          tracer.logMessage(LogLevel.INFO, "Within method_3.", getClass(), "method_1()");
          tracer.out().printfIndentln("This output goes to /dev/null.");
          throw new java.lang.RuntimeException("This is a test.");
        }
        finally {
          tracer.wayout();
          Assert.assertTrue(tracer.out() instanceof NullPrintStream);
        }
      }
    }
    
    JDKLoggingRouter jdkLoggingRouter = new JDKLoggingRouter();
    Dummy dummy = new Dummy(jdkLoggingRouter);
    dummy.method_0();
    try {
      dummy.method_1();
    }
    catch (Exception ex) {
      jdkLoggingRouter.logException(LogLevel.WARNING, ex, getClass(), "loggingRouter()");
    }
    
    Assert.assertTrue(new File(PATH_TO_LOGFILE).exists());
    List<String> lines = Files.readAllLines(new File(PATH_TO_LOGFILE).toPath(), Charset.defaultCharset());
    Assert.assertTrue("Expected four lines.", lines.size() == 4);
    String[] expectedStrings = {"Within method_1.", "Within method_2.", "Within method_3.", "This is a test."};
    int i=0;
    for (String line : lines) {
      Assert.assertTrue("Expected a suffix '" + expectedStrings[i], line.endsWith(expectedStrings[i++]));
    }
  }
}
