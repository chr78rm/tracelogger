package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.file.FileTracer;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExampleUnit5 implements WithAssertions {
    public static final Path LOGDIR = Path.of(".", "log", "examples");
    final private BannerPrinter bannerPrinter = new BannerPrinter();

    @BeforeAll
    void printHeader() {
        this.bannerPrinter.startUnit(getClass());
    }

    @BeforeEach
    void init() throws IOException {
        System.out.printf("%nResetting TracerFactory ...%n");
        TracerFactory.getInstance().reset();

        final Path EMPTY_LOG = LOGDIR.resolve("empty.log");
        DirectoryStream.Filter<Path> filter = path -> (path.getFileName().toString().endsWith(".log") && !path.equals(EMPTY_LOG));
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(LOGDIR, filter)) {
            for (Path path : stream) {
                System.out.printf("Deleting '%s' ...%n", path);
                Files.delete(path);
            }
        }
    }

    @Test
    void tracerByName() throws IOException, TracerFactory.Exception {
        this.bannerPrinter.start("tracerByName", getClass());

        TracerFactory.getInstance().readConfiguration(Path.of(".", "config", "ExampleConfig_1.xml").toFile());
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

        Path path = LOGDIR.resolve("ExampleTracer.log");
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.isRegularFile(path)).isTrue();
        List<String> lines = Files.readAllLines(path);
        assertThat(lines).hasSize(14);
    }

    @Test
    void tracerByThread() throws IOException, TracerFactory.Exception, InterruptedException {
        this.bannerPrinter.start("tracerByThread", getClass());

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

        TracerFactory.getInstance().readConfiguration(Path.of(".", "config", "ExampleConfig_2.xml").toFile());
        TracerFactory.getInstance().openPoolTracer();
        try {
            final int THREAD_NUMBER = 3;
            final int ITERATIONS = 100;
            final int TIMEOUT = 5;
            List<Future<Integer>> futures = new ArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUMBER, new ThreadFactory() {
                final AtomicInteger threadNr = new AtomicInteger();

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
                        assert future.get() == i;
                        i++;
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

    @Test
    void simpleUse() {
        this.bannerPrinter.start("simpleUse", getClass());

        final FileTracer tracer = new FileTracer("Example");
        tracer.setLogDirPath(LOGDIR);
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
}
