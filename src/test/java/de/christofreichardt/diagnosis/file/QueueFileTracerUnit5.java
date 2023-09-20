package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.*;
import de.christofreichardt.diagnosis.io.NullPrintStream;
import de.christofreichardt.diagnosis.io.TracePrintStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueueFileTracerUnit5 implements WithAssertions {
    public static final Path LOGDIR = Path.of(".", "log", "queue");
    final private BannerPrinter bannerPrinter = new BannerPrinter();

    @BeforeAll
    void printHeader() {
        this.bannerPrinter.startUnit(getClass());
    }

    @BeforeEach
    void init() throws IOException {
        System.out.printf("%nResetting JDK14 LogManager ...%n");
        LogManager.getLogManager().reset();

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

    static class Consumer implements Callable<Boolean>, WithAssertions, Traceable {
        final int id;
        final boolean activeTracing;

        public Consumer(int i, boolean activeTracing) {
            this.id = i;
            this.activeTracing = activeTracing;
        }

        @Override
        public Boolean call() throws java.lang.Exception {
            if (this.activeTracing) {
                prepareTracing();
            } else {
                QueueTracer<?> tracer = TracerFactory.getInstance().getCurrentQueueTracer();
                assertThat(tracer).isInstanceOf(QueueNullTracer.class);
                Dummy dummy = new Dummy(tracer);
                dummy.method_0();
            }
//          if (this.id == 67)
//            return false;
            return true;
        }

        void prepareTracing() {
            AbstractTracer tracer = TracerFactory.getInstance().takeTracer();
            assertThat(getCurrentTracer()).isEqualTo(tracer);
            tracer.initCurrentTracingContext();
            tracer.entry("void", this, "prepareTracing()");
            try {
                Dummy dummy = new Dummy(tracer);
                dummy.method_0();
            } finally {
                tracer.wayout();
                assertThat(getCurrentTracer()).isInstanceOf(QueueNullTracer.class);
            }
        }

        @Override
        public AbstractTracer getCurrentTracer() {
            return TracerFactory.getInstance().getCurrentQueueTracer();
        }

        class Dummy implements WithAssertions, Traceable {
            final AbstractTracer tracer;

            public Dummy(AbstractTracer tracer) {
                this.tracer = tracer;
            }

            void method_0() {
                AbstractTracer tracer = getCurrentTracer();
                tracer.entry("void", this, "method_0");
                try {
                    assertThat(this.tracer).isEqualTo(tracer);
                    tracer.logMessage(LogLevel.INFO, String.format("Consumer[%d] is running ...", Consumer.this.id), getClass(), "method_0");
                    tracer.out().printfIndentln("Within method_0() ...");
                    if (Consumer.this.activeTracing) {
                        assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
                    } else {
                        assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
                    }
                    method_1();
                } finally {
                    tracer.wayout();
                }
            }

            void method_1() {
                AbstractTracer tracer = getCurrentTracer();
                tracer.entry("void", this, "method_1");
                try {
                    assertThat(this.tracer).isEqualTo(tracer);
                    tracer.out().printfIndentln("Within method_1() ...");
                    if (Consumer.this.activeTracing) {
                        assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
                    } else {
                        assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
                    }
                    method_2();
                } finally {
                    tracer.wayout();
                }
            }

            void method_2() {
                AbstractTracer tracer = getCurrentTracer();
                if (Consumer.this.activeTracing) {
                    assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
                } else {
                    assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
                }
                tracer.entry("void", this, "method_2");
                try {
                    assertThat(this.tracer).isEqualTo(tracer);
                    tracer.out().printfIndentln("Within method_2() ..."); // shouldn't be printed since the debuglevel is exceeded.
                    assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
                } finally {
                    tracer.wayout();
                    if (Consumer.this.activeTracing) {
                        assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);
                    } else {
                        assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
                    }
                }
            }

            @Override
            public AbstractTracer getCurrentTracer() {
                return TracerFactory.getInstance().getCurrentQueueTracer();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void simpleUse(boolean activeTracing) throws IOException, TracerFactory.Exception, InterruptedException, TimeoutException {
        this.bannerPrinter.start("simpleUse", getClass());
        System.out.printf("activeTracing = %b%n", activeTracing);

        try (FileInputStream inputStream = new FileInputStream(Path.of(".", "config", "logging_2.properties").toFile())) {
            LogManager.getLogManager().readConfiguration(inputStream);
        }

        TracerFactory.getInstance().readConfiguration(Path.of(".", "config", "TraceConfig.xml").toFile());
        boolean allOpened = TracerFactory.getInstance().openQueueTracer();
        assertThat(allOpened).isTrue();
        final int THREAD_NUMBER = 10, ITERATIONS = 250, TIMEOUT = 5;
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUMBER);
            try {
                for (int i = 0; i < ITERATIONS; i++) {
                    futures.add(executorService.submit(new Consumer(i, activeTracing)));
                }
                try {
                    for (Future<Boolean> future : futures) {
                        assertThat(future.get(TIMEOUT, TimeUnit.SECONDS)).isTrue();
                    }
                } catch (ExecutionException ex) {
                    ex.getCause().printStackTrace(System.err);
                    fail(ex.getMessage());
                }
            } finally {
                executorService.shutdown();
                assertThat(executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS)).isTrue();
            }
        } finally {
            boolean allClosed = TracerFactory.getInstance().closeQueueTracer();
            assertThat(allClosed).isTrue();
        }

        String[] expectedTraceLogfiles = {"QueueTracer0.log", "QueueTracer1.log", "QueueTracer2.log", "QueueTracer3.log", "QueueTracer4.log"};
        for (String expectedTraceLogfile : expectedTraceLogfiles) {
            Path pathToTraceLog = LOGDIR.resolve(expectedTraceLogfile);
            assertThat(Files.exists(pathToTraceLog)).isTrue();
            assertThat(Files.isRegularFile(pathToTraceLog)).isTrue();
        }
        Path pathToJDK14Logfile = LOGDIR.resolve("jdk14-test.0.log");
        assertThat(Files.exists(pathToJDK14Logfile) && Files.isRegularFile(pathToJDK14Logfile)).isEqualTo(!activeTracing);
        Set<String> expectedConsumers = new HashSet<>();
        for (int i=0; i<ITERATIONS; i++) {
            expectedConsumers.add(String.format("Consumer[%d]", i));
        }
        Pattern pattern = Pattern.compile("Consumer\\[[0-9]{1,3}]");
        if (activeTracing) {
            Set<String> actualConsumers = new HashSet<>();
            long count = 0;
            for (String expectedTraceLogfile : expectedTraceLogfiles) {
                Set<String> consumers = Files.readAllLines(LOGDIR.resolve(expectedTraceLogfile)).stream()
                        .filter(line -> line.startsWith("| INFO |"))
                        .map(line -> {
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find()) {
                                return line.substring(matcher.start(), matcher.end());
                            } else {
                                return line;
                            }
                        })
                        .collect(Collectors.toSet());
                count += consumers.size();
                actualConsumers.addAll(consumers);
            }
            assertThat(count).isEqualTo(ITERATIONS);
            assertThat(actualConsumers).isEqualTo(expectedConsumers);
        } else {
            Set<String> actualConsumers = Files.readAllLines(pathToJDK14Logfile).stream()
                    .map(line -> {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            return line.substring(matcher.start(), matcher.end());
                        } else {
                            return line;
                        }
                    })
                    .collect(Collectors.toSet());
            assertThat(actualConsumers).hasSize(ITERATIONS);
            assertThat(actualConsumers).isEqualTo(expectedConsumers);
        }
    }
}
