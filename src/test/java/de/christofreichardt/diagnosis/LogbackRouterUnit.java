/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import de.christofreichardt.diagnosis.io.NullPrintStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christof Reichardt
 */
public class LogbackRouterUnit {
  public static final String PATH_TO_LOGDIR = "." + File.separator + "log" + File.separator + "logback";
  public static final String PATH_TO_LOGFILE = PATH_TO_LOGDIR + File.separator + "logback-test.log";
  
  final private BannerPrinter bannerPrinter = new BannerPrinter();
  
  @Before
  public void setup() throws IOException, JoranException {
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
    
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(context);
    context.reset();
    File configFile = new File("." + File.separator + "config" + File.separator + "logback.xml");
    configurator.doConfigure(configFile);
    StatusPrinter.print(context);
  }

  @Test
  public void loggingRouter() throws IOException {
    this.bannerPrinter.start("loggingRouter", getClass());
    
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
    
    LogbackRouter logbackRouter = new LogbackRouter();
    Dummy dummy = new Dummy(logbackRouter);
    dummy.method_0();
    try {
      dummy.method_1();
    }
    catch (Exception ex) {
      logbackRouter.logException(LogLevel.WARNING, ex, getClass(), "loggingRouter()");
    }
    Assert.assertTrue(new File(PATH_TO_LOGFILE).exists());
    List<String> lines = Files.readAllLines(new File(PATH_TO_LOGFILE).toPath(), Charset.defaultCharset());
    Assert.assertTrue("Expected at least four lines.", lines.size() >= 4);
    String[] expectedStrings = {"Within method_1.", "Within method_2.", "Within method_3.", "This is a test."};
    int i = 0;
    for (String expectedString : expectedStrings) {
      Assert.assertTrue("Expected a suffix '" + expectedString, lines.get(i++).endsWith(expectedString));
    }
  }
}
