/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.file.FileTracer;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Developer
 */
public class ExampleUnit {
  
  public static final String PATH_TO_LOGDIR = "." + File.separator + "log" + File.separator + "examples";
  final private BannerPrinter bannerPrinter = new BannerPrinter();
  
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
   * Tracer by name - strategy.
   * 
   * @throws de.christofreichardt.diagnosis.TracerFactory.Exception
   * @throws FileNotFoundException
   * @throws IOException 
   */
  @Ignore
  @Test
  public void example_1() throws TracerFactory.Exception, FileNotFoundException, IOException {
    this.bannerPrinter.start("example_1", getClass());
    
    File configFile = new File("." + File.separator + "config" + File.separator + "ExampleConfig_1.xml");
    TracerFactory.getInstance().reset();
    TracerFactory.getInstance().readConfiguration(configFile);
    final AbstractTracer tracer = TracerFactory.getInstance().getTracer("ExampleTracer");
    tracer.open();
    try {
      class Foo {

        final AbstractTracer tracer;

        public Foo() throws TracerFactory.Exception {
          this.tracer = TracerFactory.getInstance().getTracer("ExampleTracer"); // throws an Exception if "ExampleTracer" doesn't exist.
        }

        void bar() {
          this.tracer.entry("void", this, "bar()");
          try {
            this.tracer.out().printfIndentln("Within bar().");
            baz();
          }
          finally {
            this.tracer.wayout();
          }
        }

        void baz() {
          this.tracer.entry("void", this, "baz()");
          try {
            this.tracer.out().printfIndentln("Within baz().");
          }
          finally {
            this.tracer.wayout();
          }
        }
      }
      Foo foo = new Foo();
      foo.bar(); // nothing will be printed because no tracing context has been provided
      tracer.initCurrentTracingContext(); // the configured tracing context will be used
      foo.bar(); // this generates output
    }
    finally {
      tracer.close();
    }
    
    File logFile = new File(PATH_TO_LOGDIR + File.separator + "ExampleTracer.log");
    assertTrue("Expected a '" + logFile.getPath() + "' but found none.", logFile.exists());
    List<String> lines = Files.readAllLines(logFile.toPath(), Charset.defaultCharset());
    assertTrue("Expected 14 lines.", lines.size() == 14);
  }
  
  /**
   * Tracer by thread - strategy.
   * 
   * @throws de.christofreichardt.diagnosis.TracerFactory.Exception
   * @throws FileNotFoundException
   * @throws InterruptedException 
   */
  @Test
  public void example_2() throws TracerFactory.Exception, FileNotFoundException, InterruptedException {
    this.bannerPrinter.start("example_2", getClass());

    class Foo implements Callable<Integer>, Traceable {

      final int id;

      public Foo(int id) {
        this.id = id;
      }

      @Override
      public Integer call() throws java.lang.Exception {
        AbstractTracer tracer = getCurrentTracer();
        tracer.initCurrentTracingContext();
        tracer.entry("Integer", this, "call()");
        try {
          tracer.logMessage(LogLevel.INFO, "Consumer(" + this.id + ")", getClass(), "call()");
          bar();
          return this.id;
        }
        finally {
          tracer.wayout();
        }
      }

      void bar() {
        getCurrentTracer().entry("void", this, "bar()");
        try {
          getCurrentTracer().out().printfIndentln("Within bar().");
          baz();
        }
        finally {
          getCurrentTracer().wayout();
        }
      }

      void baz() {
        getCurrentTracer().entry("void", this, "baz()");
        try {
          getCurrentTracer().out().printfIndentln("Within baz().");
        }
        finally {
          getCurrentTracer().wayout();
        }
      }

      @Override
      public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
      }
    }
    
    File configFile = new File("." + File.separator + "config" + File.separator + "ExampleConfig_2.xml");
    TracerFactory.getInstance().readConfiguration(configFile);
    TracerFactory.getInstance().openPoolTracer();
    try {
      final int THREAD_NUMBER = 3;
      final int ITERATIONS = 100;
      final int TIMEOUT = 5;
      List<Future<Integer>> futures = new ArrayList<>();
      ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUMBER, new ThreadFactory() {
        AtomicInteger threadNr = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
          return new Thread(r, "Worker-" + this.threadNr.getAndIncrement());
        }
      });
      try {
        for (int i = 0; i < ITERATIONS; i++) {
          futures.add(executorService.submit(new Foo(i)));
        }
        try {
          int i = 0;
          for (Future<Integer> future : futures) {
            assert future.get() == i++;
          }
        }
        catch (ExecutionException ex) {
          ex.getCause().printStackTrace(System.err);
        }
      }
      finally {
        executorService.shutdown();
        assert executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
      }
    }
    finally {
      TracerFactory.getInstance().closePoolTracer();
    }
  }
  
  @Ignore
  @Test
  public void example_3() {
    this.bannerPrinter.start("example_3", getClass());
    
    final FileTracer tracer = new FileTracer("Example");
    tracer.setLogDirPath(new File(PATH_TO_LOGDIR).toPath());
    tracer.open();
    try {
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
      foo.bar(); // nothing will be printed because no tracing context has been provided    
      tracer.initCurrentTracingContext(2, true);
      foo.bar(); // this generates output                                                   
    }
    finally {
      tracer.close();
    }
  }
  
  public void dummy() throws TracerFactory.Exception, FileNotFoundException, InterruptedException {
  }
}
