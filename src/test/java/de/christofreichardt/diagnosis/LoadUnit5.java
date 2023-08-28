package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.file.FileTracer;
import de.christofreichardt.diagnosis.io.NullPrintStream;
import de.christofreichardt.diagnosis.io.TracePrintStream;
import de.christofreichardt.diagnosis.net.NetTracer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoadUnit5 implements WithAssertions {
    public static final Path LOGDIR = Path.of(".", "log", "load");
    final private BannerPrinter bannerPrinter = new BannerPrinter();

    @BeforeAll
    void printHeader() {
        this.bannerPrinter.startUnit(getClass());
    }

    @BeforeEach
    void init() throws IOException {
        final Path EMPTY_LOG = LOGDIR.resolve("empty.log");
        DirectoryStream.Filter<Path> filter = path -> (path.getFileName().toString().endsWith(".log") && !path.equals(EMPTY_LOG));
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(LOGDIR, filter)) {
            for (Path path : stream) {
                System.out.printf("Deleting '%s' ...%n", path);
                Files.delete(path);
            }
        }
    }

    static class Log implements WithAssertions {
        final List<String> lines;

        Log(List<String> lines) {
            this.lines = lines;
        }

        void verify() {
            List<String> warnings = lines.stream()
                    .filter(line -> line.startsWith("* WARNING *"))
                    .collect(Collectors.toList());
            assertThat(
                    warnings.stream()
                            .filter(warning -> warning.endsWith("\"Eine Exception zu Testzwecken.\""))
                            .count()
            ).isEqualTo(300);
            assertThat(
                    warnings.stream()
                            .filter(warning -> warning.endsWith("\"Nur zum testen.\""))
                            .count()
            ).isEqualTo(3);
            assertThat(
                    lines.stream()
                            .filter(line -> line.startsWith("* SEVERE *"))
                            .count()
            ).isEqualTo(0);
        }
    }

    @Test
    void fileTracer() throws InterruptedException, BrokenBarrierException, IOException, TimeoutException {
        this.bannerPrinter.start("fileTracer", getClass());

        FileTracer fileTracer = new FileTracer("Test");
        fileTracer.setLogDirPath(LOGDIR);
        runScenario(fileTracer, new TestClass(fileTracer));
        List<String> lines = Files.readAllLines(LOGDIR.resolve("Test.log"), Charset.defaultCharset());
        Log log = new Log(lines);
        log.verify();
    }

    @Test
    void netTracer() throws BrokenBarrierException, InterruptedException, ExecutionException, TimeoutException {
        this.bannerPrinter.start("netTracer", getClass());

        CountDownLatch countDownLatch = new CountDownLatch(1);
        final int SOCKET_TIMEOUT = 5000, RECEIVER_TIMEOUT = 2500, SERVICE_TIMEOUT = 2500;

        class Receiver implements Callable<List<String>> {
            int portNo = -1;
            final List<String> lines = new ArrayList<>();

            @Override
            public List<String> call() throws java.lang.Exception {
                try (ServerSocket listener = new ServerSocket(0)) {
                    this.portNo = listener.getLocalPort();
                    listener.setSoTimeout(SOCKET_TIMEOUT);
                    countDownLatch.countDown();
                    try (Socket socket = listener.accept()) {
                        LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
                        String line;
                        while ((line = lineNumberReader.readLine()) != null) {
                            this.lines.add(line);
                        }
                    }
                }

                return this.lines;
            }
        }

        Receiver receiver = new Receiver();
        NetTracer netTracer = new NetTracer("Test");
        netTracer.setHostName("localhost");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<List<String>> future = executorService.submit(receiver);
            boolean isReceiverUp = countDownLatch.await(RECEIVER_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!isReceiverUp) {
                throw new RuntimeException("No receiver.");
            }
            netTracer.setPortNo(receiver.portNo);
            runScenario(netTracer, new TestClass(netTracer));
            List<String> lines = future.get(RECEIVER_TIMEOUT, TimeUnit.MILLISECONDS);
            Log log = new Log(lines);
            log.verify();
        } finally {
            executorService.shutdown();
            boolean terminated = executorService.awaitTermination(SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
            System.out.printf("terminated = %b%n", terminated);
            if (!terminated) {
                System.out.printf("Aborting ...%n");
                executorService.shutdownNow();
                terminated = executorService.awaitTermination(SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
                System.out.printf("terminated = %b%n", terminated);
            }
        }
    }

    @Test
    void corruptedStack() throws BrokenBarrierException, InterruptedException, IOException, TimeoutException {
        this.bannerPrinter.start("corruptedStack", getClass());

        FileTracer fileTracer = new FileTracer("Test");
        fileTracer.setLogDirPath(LOGDIR);
        TestClass testObject = new TestClass(fileTracer);
        testObject.setEmptyStackCorruption(true);
        runScenario(fileTracer, testObject);
        List<String> lines = Files.readAllLines(LOGDIR.resolve("Test.log"), Charset.defaultCharset());
        Set<String> enabledThreads = new HashSet<>(Arrays.asList("main", "TestThread-0", "TestThread-1"));
        Set<String> disabledThreads = new HashSet<>();
        for (String line : lines) {
            if (line.startsWith("* SEVERE *")) {
                assertThat(line).endsWith("\"Stack is corrupted. Tracing is off.\"");
                String foundThread = enabledThreads.stream()
                        .filter(thread -> line.contains(thread))
                        .findFirst()
                        .orElseThrow();
                enabledThreads.remove(foundThread);
                disabledThreads.add(foundThread);
            } else if (line.startsWith("ENTRY--") || line.startsWith("RETURN-")) {
                assertThat(
                        disabledThreads.stream()
                                .filter(thread -> line.contains(thread))
                                .findFirst()
                                .isEmpty()
                ).isTrue();
            }
        }
        assertThat(enabledThreads).isEmpty();
        assertThat(disabledThreads).contains("main", "TestThread-0", "TestThread-1");
        assertThat(fileTracer.out()).isInstanceOf(NullPrintStream.class);
    }

    @Test
    void stackOverflow() throws BrokenBarrierException, InterruptedException, IOException, TimeoutException {
        this.bannerPrinter.start("stackOverflow", getClass());

        FileTracer fileTracer = new FileTracer("Test");
        fileTracer.setLogDirPath(LOGDIR);
        TestClass testObject = new TestClass(fileTracer);
        testObject.setStackOverFlowCorruption(true);
        runScenario(fileTracer, testObject);
        List<String> lines = Files.readAllLines(LOGDIR.resolve("Test.log"), Charset.defaultCharset());
        Set<String> enabledThreads = new HashSet<>(Arrays.asList("main", "TestThread-0", "TestThread-1"));
        Set<String> disabledThreads = new HashSet<>();
        for (String line : lines) {
            if (line.startsWith("* SEVERE *")) {
                assertThat(line).endsWith("\"Stacksize is exceeded. Tracing is off.\"");
                String foundThread = enabledThreads.stream()
                        .filter(thread -> line.contains(thread))
                        .findFirst()
                        .orElseThrow();
                enabledThreads.remove(foundThread);
                disabledThreads.add(foundThread);
            } else if (line.startsWith("ENTRY--") || line.startsWith("RETURN-")) {
                assertThat(
                        disabledThreads.stream()
                                .filter(thread -> line.contains(thread))
                                .findFirst()
                                .isEmpty()
                ).isTrue();
            }
        }
        assertThat(enabledThreads).isEmpty();
        assertThat(disabledThreads).contains("main", "TestThread-0", "TestThread-1");
        assertThat(fileTracer.out()).isInstanceOf(NullPrintStream.class);
    }

    static class MyThreadFactory implements ThreadFactory {
        final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, String.format("TestThread-%d", this.counter.getAndIncrement()));
        }
    }

    void runScenario(AbstractTracer tracer, TestClass testObject) throws InterruptedException, BrokenBarrierException, TimeoutException {
        try {
            tracer.open();
            assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
            tracer.out().printIndentln("This is a test."); // will be ignored
            tracer.initCurrentTracingContext(5, true);
            assertThat(tracer.out()).isInstanceOf(TracePrintStream.class);

            tracer.entry("void", this, "runScenario(AbstractTracer tracer, TestClass testClass)");
            try {
                tracer.out().printfIndentln("tracer.getName() = %s", tracer.getName());
                tracer.out().printIndentln("This is a test.");
                final int WORKER_THREADS = 2, TIME_OUT = 5;
                CyclicBarrier cyclicBarrier = new CyclicBarrier(WORKER_THREADS + 1);
                List<Future<?>> futures = new ArrayList<>();
                ExecutorService executorService = Executors.newFixedThreadPool(WORKER_THREADS, new MyThreadFactory());
                try {
                    for (int i = 0; i < WORKER_THREADS; i++) {
                        TestRunnable testRunnable = new TestRunnable(tracer);
                        testRunnable.setTestObject(testObject);
                        testRunnable.setCyclicBarrier(cyclicBarrier);
                        futures.add(executorService.submit(testRunnable));
                    }
                    cyclicBarrier.await(TIME_OUT, TimeUnit.SECONDS);
                    testObject.performTests();
                    futures.forEach(future -> {
                        try {
                            future.get(TIME_OUT, TimeUnit.SECONDS);
                        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                } finally {
                    executorService.shutdown();
                    boolean terminated = executorService.awaitTermination(TIME_OUT, TimeUnit.SECONDS);
                    assertThat(terminated).isTrue();
                }
            } finally {
                tracer.wayout();
            }
        } finally {
            tracer.close();
        }
    }
}
