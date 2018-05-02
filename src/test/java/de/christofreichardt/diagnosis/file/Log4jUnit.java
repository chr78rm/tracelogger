/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.BannerPrinter;
import de.christofreichardt.diagnosis.SimpleDummy;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
public class Log4jUnit {
  
  public static final String PATH_TO_LOGDIR = "." + File.separator + "log" + File.separator + "log4j";
  public static final String PATH_TO_LOGFILE = PATH_TO_LOGDIR + File.separator + "log4j-test.log";
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
    LogManager.shutdown();
  }

  @Test
  public void log4jConfig() {
    this.bannerPrinter.start("log4jConfig", getClass());
    
    DOMConfigurator.configure("." + File.separator + "config" + File.separator + "log4j.xml");
    Logger myLogger = Logger.getLogger(getClass());
//    myLogger.info("This is a test.");
    Assert.assertTrue("", new File(PATH_TO_LOGFILE).exists());
  }

  @Test
  public void debugLevel() throws IOException {
    this.bannerPrinter.start("debugLevel", getClass());
    
    DOMConfigurator.configure("." + File.separator + "config" + File.separator + "log4j.xml");
    FileTracerLog4jTee fileTracerLog4jTee = new FileTracerLog4jTee(("Test"));
    fileTracerLog4jTee.setLogDirPath(new File(PATH_TO_LOGDIR).toPath());
    SimpleDummy simpleDummy = new SimpleDummy(fileTracerLog4jTee);
    try {
      fileTracerLog4jTee.open();
      simpleDummy.method_0();
      fileTracerLog4jTee.initCurrentTracingContext(2, true);
      simpleDummy.method_1();
    }
    finally {
      fileTracerLog4jTee.close();
    }
    
    Assert.assertTrue(new File(PATH_TO_LOGDIR + File.separator + "Test.log").exists());
    String[] expectedStrings = {"ENTRY--method_1()", "ENTRY--method_2()", "ENTRY--method_3()", "This is a test.", "RETURN-method_3()",
      "RETURN-method_2()", "RETURN-method_1()"};
    List<String> lines = Files.readAllLines(new File(PATH_TO_LOGFILE).toPath(), Charset.defaultCharset());
    Assert.assertTrue("Expected " + expectedStrings.length + " lines.", lines.size() == expectedStrings.length);
    int i=0;
    for (String line : lines) {
      Assert.assertTrue("Expected a suffix '" + expectedStrings[i], line.endsWith(expectedStrings[i++]));
    }
  }
}
