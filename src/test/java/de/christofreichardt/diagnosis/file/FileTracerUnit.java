/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.BannerPrinter;
import de.christofreichardt.diagnosis.io.NullPrintStream;
import de.christofreichardt.diagnosis.io.TracePrintStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
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
public class FileTracerUnit {
  final private BannerPrinter bannerPrinter = new BannerPrinter();
  
  @Before
  public void setup() throws IOException {
    File logDir = new File("." + File.separator + "log");
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
  
  /**
   * Open- and close-operations on a manually created FileTracer.
   */
  @Test
  public void simpleFileTracer() {
    this.bannerPrinter.start("simpleFileTracer", getClass());
    
    FileTracer fileTracer = new FileTracer("Test");
    Assert.assertTrue(!fileTracer.isOpened());
    fileTracer.open();
    Assert.assertTrue(fileTracer.isOpened());
    fileTracer.close();
    Assert.assertTrue(!fileTracer.isOpened());
    File logFile = new File("." + File.separator + "log" + File.separator + "Test.log");
    Assert.assertTrue("Expecting an existing " + logFile.getAbsolutePath() + "'.", logFile.exists());
  }
  
  /**
   * Triggers log file splitting on a manually created FileTracer.
   */
  @Test
  public void logFileRolling() {
    this.bannerPrinter.start("logFileRolling", getClass());
    
    final String TRACER_NAME = "Test";
    final FileTracer fileTracer = new FileTracer(TRACER_NAME);
    final long LIMIT = 8192;
    final int NUMBER_OF_ROLLS = 3;
    final String TESTLINE = System.lineSeparator().length() == 2
        ? "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567"
        : "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678";
    final long CHARS_PER_LINE = TESTLINE.length();
    final long N = LIMIT/CHARS_PER_LINE + 1;
    fileTracer.setByteLimit(LIMIT);
    try {
      fileTracer.open();
      fileTracer.initCurrentTracingContext(5, true);
      
      class Dummy {
        void method() {
          for (int i = 0; i < N; i++) {
            fileTracer.out().println(TESTLINE);
            fileTracer.out().flush();
          }
        }
      }
      
      for (int i=0; i<NUMBER_OF_ROLLS; i++) {
        Dummy dummy = new Dummy();
        dummy.method();
      }
      
      File logDir = new File("." + File.separator + "log");
      Assert.assertTrue("Expected a '" + TRACER_NAME + ".log'", new File(logDir, TRACER_NAME + ".log").exists());
      for (int i=0; i<NUMBER_OF_ROLLS; i++) {
        Assert.assertTrue("Expected a '" + TRACER_NAME + "." + i + ".log'", new File(logDir, TRACER_NAME + "." + i + ".log").exists());
      }
    }
    finally {
      fileTracer.close();
    }
  }
  
  /**
   * Tests output redirection with a manually provided tracing context.
   * @throws java.io.IOException
   */
  @Test
  public void debugLevel() throws IOException {
    this.bannerPrinter.start("debugLevel", getClass());
    
    final String TRACERNAME = "SimpleTest";
    final FileTracer fileTracer = new FileTracer(TRACERNAME);
    try {
      fileTracer.open();

      class Dummy {
        void method_0() {
          Assert.assertTrue(fileTracer.out() instanceof NullPrintStream);
          fileTracer.entry("void", this, "method_0()");
          try {
            Assert.assertTrue(fileTracer.out() instanceof NullPrintStream);
          }
          finally {
            fileTracer.wayout();
            Assert.assertTrue(fileTracer.out() instanceof NullPrintStream);
          }
        }
        void method_1() {
          Assert.assertTrue(fileTracer.out() instanceof TracePrintStream);
          fileTracer.entry("void", this, "method_1()");
          try {
            Assert.assertTrue(fileTracer.out() instanceof TracePrintStream);
            method_2();
          }
          finally {
            fileTracer.wayout();
            Assert.assertTrue(fileTracer.out() instanceof TracePrintStream);
          }
        }
        void method_2() {
          Assert.assertTrue(fileTracer.out() instanceof TracePrintStream);
          fileTracer.entry("void", this, "method_2()");
          try {
            Assert.assertTrue(fileTracer.out() instanceof TracePrintStream);
            method_3();
          }
          finally {
            fileTracer.wayout();
            Assert.assertTrue(fileTracer.out() instanceof TracePrintStream);
          }
        }
        void method_3() {
          Assert.assertTrue(fileTracer.out() instanceof TracePrintStream);
          fileTracer.entry("void", this, "method_3()");
          try {
            Assert.assertTrue(fileTracer.out() instanceof NullPrintStream);
            fileTracer.out().printfIndentln("This is a test.");
          }
          finally {
            fileTracer.wayout();
            Assert.assertTrue(fileTracer.out() instanceof TracePrintStream);
          }
        }
      }

      Dummy dummy = new Dummy();
      dummy.method_0();
      fileTracer.initCurrentTracingContext(2, true);
      dummy.method_1();
    }
    finally {
      fileTracer.close();
    }
    
    File traceLogFile = new File("." + File.separator + "log" + File.separator + TRACERNAME + ".log");
    Assert.assertTrue("TraceLogFile missing.", traceLogFile.exists());
    Pattern pattern = Pattern.compile("void Dummy\\[[0-9]+\\]\\.method_[123]\\(\\)");
    try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(traceLogFile))) {
      String line;
      while ((line = lineNumberReader.readLine()) != null) {
//        System.out.printf("line[%d] = %s%n", lineNumberReader.getLineNumber(), line);
        if (lineNumberReader.getLineNumber() >= 6  &&  lineNumberReader.getLineNumber() <= 11) {
          Matcher matcher = pattern.matcher(line);
          Assert.assertTrue("Unexpected output: " + line, matcher.find());
        }
      }
    }
  }
  
  @Test
  public void example() {
    this.bannerPrinter.start("example", getClass());
    
    final AbstractTracer tracer = new FileTracer("Example");
    try {
      tracer.open();
      class Foo {

        void bar() {
          tracer.entry("void", this, "bar()");
          try {
            tracer.out().printfIndentln("This is an example.");
          }
          finally {
            tracer.wayout();
          }
        }
      }
      Foo foo = new Foo();
      foo.bar(); // nothing will be printed because no tracing context is provided
      tracer.initCurrentTracingContext(2, true);
      foo.bar(); // this will generate output
    }
    finally {
      tracer.close();
    }
    Assert.assertTrue("Example.log should exist.", new File("." + File.separator + "log" + File.separator + "Example.log").exists());
  }
}
