package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.file.FileTracer;
import de.christofreichardt.diagnosis.net.NetTracer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TracerFactoryUnit5 implements WithAssertions {
    public static final Path LOGDIR = Path.of(".", "log");
    final private BannerPrinter bannerPrinter = new BannerPrinter();

    @BeforeAll
    void printHeader() {
        this.bannerPrinter.startUnit(getClass());
    }

    @BeforeEach
    void init() throws IOException {
        System.out.printf("%nResetting TracerFactory ...%n");
        TracerFactory.getInstance().reset();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(LOGDIR, "*.log")) {
            for (Path path : stream) {
                System.out.printf("Deleting '%s' ...%n", path);
                Files.delete(path);
            }
        }
    }

    @Test
    void instance() {
        this.bannerPrinter.start("instance", getClass());

        TracerFactory instance = TracerFactory.getInstance();
        assertThat(instance).isNotNull();
    }

    @Test
    void defaultTracer() {
        this.bannerPrinter.start("defaultTracer", getClass());

        AbstractTracer defaultTracer = TracerFactory.getInstance().getDefaultTracer();
        assertThat(defaultTracer).isNotNull();
        assertThat(defaultTracer).isInstanceOf(NullTracer.class);
        assertThat(defaultTracer).isEqualTo(TracerFactory.NULLTRACER); // must be so since we are resetting the TracerFactory instance before each test case
    }

    @Test
    void configuration() throws IOException, TracerFactory.Exception, ExecutionException, InterruptedException, TimeoutException {
        this.bannerPrinter.start("configuration", getClass());

        Path config = Path.of(".", "config", "TraceConfig.xml");
        TracerFactory.getInstance().readConfiguration(config.toFile());

        // check correct tracer class and name
        Class<?>[] tracerClasses = {FileTracer.class, FileTracer.class, NetTracer.class, NullTracer.class, FileTracer.class};
        for (int i = 0; i < tracerClasses.length; i++) {
            String tracerName = "TestTracer-" + i;
            AbstractTracer testTracer = TracerFactory.getInstance().getTracer(tracerName);
            assertThat(testTracer).isNotNull();
            assertThat(testTracer.getName()).isEqualTo(tracerName);
            assertThat(testTracer.getClass()).isEqualTo(tracerClasses[i]);
        }

        // check mapping for 'main' thread
        assertThat(Thread.currentThread().getName()).isEqualTo("main");
        AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
        assertThat(tracer.getName()).isEqualTo("TestTracer-0");

        // check mapping for worker threads
        for (int i = 0; i <= 2; i++) {
            final String THREAD_NAME = "TestThread-" + i;
            final String TRACER_NAME = "TestTracer-" + i;
            ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, THREAD_NAME));
            try {
                Future<Boolean> future = executorService.submit(() -> {
                    AbstractTracer poolTracer = TracerFactory.getInstance().getCurrentPoolTracer();
                    return TRACER_NAME.equals(poolTracer.getName());
                });
                assertThat(future.get(1, TimeUnit.SECONDS)).isTrue();
            } finally {
                executorService.shutdown();
            }
        }

        // duplicate thread name
        ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> {
            return new Thread(runnable, "TestThread-0"); // duplicate thread name(!)
        });
        try {
            Future<Boolean> future = executorService.submit(() -> {
                AbstractTracer poolTracer = TracerFactory.getInstance().getCurrentPoolTracer();
                AbstractTracer defaultTracer = TracerFactory.getInstance().getDefaultTracer();
                return poolTracer == defaultTracer;
            });
            assertThat(future.get(1, TimeUnit.SECONDS)).isTrue();
        } finally {
            executorService.shutdown();
        }

        // check queue config
        assertThat(TracerFactory.getInstance().isQueueEnabled()).isTrue();
        assertThat(TracerFactory.getInstance().getQueueSize()).isEqualTo(5);
        assertThat(TracerFactory.getInstance().getQueueTracerClassname()).isEqualTo("de.christofreichardt.diagnosis.file.QueueFileTracer");
    }

    @Test
    void unknownTracerByName() {
        this.bannerPrinter.start("unknownTracerByName", getClass());

        String tracerName = "TestTracer-0"; // unknown since we are resetting the TracerFactory instance before each test case
        assertThatExceptionOfType(TracerFactory.Exception.class).isThrownBy(
                        () -> TracerFactory.getInstance().getTracer(tracerName)
                )
                .withMessage(String.format("Unknown tracer: '%s'", tracerName));
    }

    @Test
    void unkownTracerByMapping() {
        this.bannerPrinter.start("unkownTracerByMapping", getClass());

        AbstractTracer currentTracer = TracerFactory.getInstance().getCurrentPoolTracer();
        AbstractTracer defaultTracer = TracerFactory.getInstance().getDefaultTracer();
        assertThat(currentTracer).isEqualTo(defaultTracer);
    }

    @Test
    void invalidTracerClass() {
        this.bannerPrinter.start("invalidTracerClass", getClass());

        Path config = Path.of(".", "config", "InvalidTraceConfig_1.xml");
        assertThatExceptionOfType(TracerFactory.Exception.class).isThrownBy(
                        () -> TracerFactory.getInstance().readConfiguration(config.toFile())
                )
                .withMessage(String.format("Illegal tracer class: '%s'", "java.lang.String"));
    }

    @Test
    void invalidDefaultTracerClass() {
        this.bannerPrinter.start("invalidDefaultTracerClass", getClass());

        Path config = Path.of(".", "config", "InvalidTraceConfig_2.xml");
        assertThatExceptionOfType(TracerFactory.Exception.class).isThrownBy(
                        () -> TracerFactory.getInstance().readConfiguration(config.toFile())
                )
                .withMessage("Requiring a NullTracer as default tracer!");
    }

    @Test
    void configSchemaViolated() {
        this.bannerPrinter.start("configSchemaViolated", getClass());

        Path config = Path.of(".", "config", "InvalidTraceConfig_3.xml");
        assertThatExceptionOfType(TracerFactory.Exception.class).isThrownBy(
                        () -> TracerFactory.getInstance().readConfiguration(config.toFile())
                )
                .havingCause()
                .withMessageEndingWith("The content of element 'Map' is not complete. One of '{\"http://www.christofreichardt.de/java/tracer\":Threads}' is expected.");
    }

    @Test
    void openAllAndCloseAll() throws TracerFactory.Exception, IOException, ExecutionException, InterruptedException, TimeoutException {
        this.bannerPrinter.start("openAllAndCloseAll", getClass());

        Path config = Path.of(".", "config", "TraceConfig.xml");
        TracerFactory.getInstance().readConfiguration(config.toFile());
        final int PORT_NO = ((NetTracer) TracerFactory.getInstance().getTracer("TestTracer-2")).getPortNo();
        final int TIME_OUT = 10;

        class Receiver implements Callable<Boolean> {
            @Override
            public Boolean call() throws java.lang.Exception {
                try (ServerSocket listener = new ServerSocket(PORT_NO)) {
                    try (Socket socket = listener.accept()) {
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

        String[] tracerNames = {"TestTracer-0", "TestTracer-1", "TestTracer-2", "TestTracer-3", "TestTracer-4"};
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> future = executorService.submit(new Receiver());
            try {
                TracerFactory.getInstance().openPoolTracer();
                for (String tracerName : tracerNames) {
                    assertThat(TracerFactory.getInstance().getTracer(tracerName).isOpened()).isTrue();
                }
            }
            finally {
                TracerFactory.getInstance().closePoolTracer();
            }
            assertThat(future.get(TIME_OUT, TimeUnit.SECONDS)).isTrue();
            assertThat(Files.exists(LOGDIR.resolve("TestTracer-0.log"))).isTrue();
            assertThat(Files.exists(LOGDIR.resolve("TestTracer-1.log"))).isTrue();
            assertThat(Files.exists(LOGDIR.resolve("TestTracer-3.log"))).isFalse();
            assertThat(Files.exists(LOGDIR.resolve("TestTracer-4.log"))).isTrue();
            for (String tracerName : tracerNames) {
                assertThat(TracerFactory.getInstance().getTracer(tracerName).isOpened()).isFalse();
            }
        }
        finally {
            executorService.shutdown();
        }
    }

    @Test
    void debugLevel() throws IOException, TracerFactory.Exception {
        this.bannerPrinter.start("debugLevel", getClass());

        Path config = Path.of(".", "config", "TraceConfig.xml");
        TracerFactory.getInstance().readConfiguration(config.toFile());
        final AbstractTracer tracer = TracerFactory.getInstance().getTracer("TestTracer-4");
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

    @ParameterizedTest
    @ValueSource(strings = { "DisabledQueueTraceConfig_1.xml", "DisabledQueueTraceConfig_2.xml"})
    void disabledQueueTraceConfig(String fileName) throws IOException, TracerFactory.Exception {
        this.bannerPrinter.start("disabledQueueTraceConfig", getClass());
        System.out.printf("fileName = %s%n", fileName);

        Path config = Path.of(".", "config", fileName);
        TracerFactory.getInstance().readConfiguration(config.toFile());

        assertThat(TracerFactory.getInstance().getQueueSize()).isEqualTo(0);
        assertThat(TracerFactory.getInstance().isQueueEnabled()).isFalse();
        assertThat(TracerFactory.getInstance().takeTracer()).isInstanceOf(QueueNullTracer.class);

        TracerFactory.getInstance().openQueueTracer();
        QueueTracer<? extends AbstractTracer> tracer = TracerFactory.getInstance().takeTracer();
        assertThat(tracer.getThreadMap().getCurrentStackSize()).isEqualTo(-1);
        tracer.initCurrentTracingContext(5, true);
        assertThat(tracer.getThreadMap().getCurrentStackSize()).isEqualTo(-1);
        tracer.entry("void", this, "dummy()");
        try {
            tracer.out().printfIndentln("This is a test.");
            assertThat(tracer.getThreadMap().getCurrentStackSize()).isEqualTo(-1);
        } finally {
            tracer.wayout();
        }
        assertThat(tracer.getThreadMap().getCurrentStackSize()).isEqualTo(-1);
        assertThat(TracerFactory.getInstance().offerTracer(tracer)).isFalse();

        class Consumer implements Callable<Boolean> {
            @Override
            public Boolean call() {
                final int ITERATIONS = 100;
                for (int i=0; i<ITERATIONS; i++) {
                    TracerFactory.getInstance().takeTracer();
                }
                return true;
            }
        }
        final int THREADS = 5;
        List<Future<Boolean>> results = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        for (int i=0; i<THREADS; i++) {
            results.add(executorService.submit(new Consumer()));
        }
        assertThat(results.stream().allMatch(result -> {
            try {
                return result.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new RuntimeException(ex);
            }
        })).isTrue();
    }
}
