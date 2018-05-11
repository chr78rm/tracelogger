/*
 * Copyright 2018 Christof Reichardt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.christofreichardt.diagnosis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christof Reichardt
 */
public class Log4j2RouterUnit {

  public static final String PATH_TO_LOGDIR = "." + File.separator + "log" + File.separator + "log4j2";
  public static final String PATH_TO_LOGFILE = PATH_TO_LOGDIR + File.separator + "log4j2-test.log";

  final private BannerPrinter bannerPrinter = new BannerPrinter();
  final private NullTracer nullTracer = new Log4j2Router();

  @Before
  public void setup() throws IOException {
    File logDir = new File(PATH_TO_LOGDIR);
    File[] logFiles = logDir.listFiles((File file) -> file.getName().endsWith("log") && !"empty.log".equals(file.getName()));
    for (File logFile : logFiles) {
      System.out.printf("Deleting '%s'.%n", logFile.getName());
      if (!logFile.delete()) {
        throw new IOException("Cannot delete '" + logFile.getAbsolutePath() + "'.");
      }
    }
    ((org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false)).reconfigure();
  }

  class Foo implements Traceable {

    Bar bar = new Bar();

    void doSomething() {
      AbstractTracer tracer = getCurrentTracer();
      tracer.entry("void", this, "doSomething()");

      try {
        tracer.logMessage(LogLevel.INFO, "Within doSomething() ...", getClass(), "doSomething()");
      } finally {
        tracer.wayout();
      }
    }

    void invokeBar() {
      AbstractTracer tracer = getCurrentTracer();
      tracer.entry("void", this, "invokeBar()");

      try {
        tracer.logMessage(LogLevel.INFO, "Within invokeBar() ...", getClass(), "invokeBar()");
        this.bar.throwSomething();
      } finally {
        tracer.wayout();
      }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
      return Log4j2RouterUnit.this.nullTracer;
    }

  }

  class Bar implements Traceable {

    void throwSomething() {
      AbstractTracer tracer = getCurrentTracer();
      tracer.entry("void", this, "throwSomething()");

      try {
        tracer.logMessage(LogLevel.INFO, "Within throwSomething() ...", getClass(), "throwSomething()");
        throw new RuntimeException("This is a test.");
      } finally {
        tracer.wayout();
      }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
      return Log4j2RouterUnit.this.nullTracer;
    }
  }

  @Test
  public void loggingRouter() throws IOException {
    this.bannerPrinter.start("loggingRouter", getClass());

    File logFile = new File(PATH_TO_LOGFILE);
    Foo foo = new Foo();
    foo.doSomething();
    try {
      foo.invokeBar();
    } catch (Exception ex) {
      this.nullTracer.logException(LogLevel.INFO, ex, getClass(), "loggingRouter()");
    }
    Assert.assertTrue(logFile.exists());
    List<String> lines = Files.readAllLines(new File(PATH_TO_LOGFILE).toPath(), Charset.defaultCharset());
    Assert.assertTrue("Expected at least four lines.", lines.size() >= 4);
    String[] expectedStrings = {"Within doSomething() ...", "Within invokeBar() ...", "Within throwSomething() ...", 
      "Catched: java.lang.RuntimeException: This is a test."};
    int i = 0;
    for (String expectedString : expectedStrings) {
      Assert.assertTrue("Expected a suffix '" + expectedString, lines.get(i++).endsWith(expectedString));
    }
  }
}
