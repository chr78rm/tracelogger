package de.christofreichardt.diagnosis;

import ch.qos.logback.core.joran.spi.JoranException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Log4j2RouterUnit5 implements WithAssertions {
    public static final Path LOGDIR = Path.of(".", "log", "log4j2");
    public static final Path LOGFILE = LOGDIR.resolve("log4j2-test.log");
    final private BannerPrinter bannerPrinter = new BannerPrinter();
    final private NullTracer nullTracer = new Log4j2Router();

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

        System.out.printf("%nReconfiguring LoggerContext ...%n");
        ((org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false)).reconfigure();
    }

    class Foo implements Traceable {

        Log4j2RouterUnit5.Bar bar = new Log4j2RouterUnit5.Bar();

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
            return Log4j2RouterUnit5.this.nullTracer;
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
            return Log4j2RouterUnit5.this.nullTracer;
        }
    }

    @Test
    void loggingRouter() throws IOException {
        this.bannerPrinter.start("loggingRouter", getClass());

        Log4j2RouterUnit5.Foo foo = new Log4j2RouterUnit5.Foo();
        foo.doSomething();
        try {
            foo.invokeBar();
        } catch (Exception ex) {
            this.nullTracer.logException(LogLevel.WARNING, ex, getClass(), "loggingRouter()");
        }
        assertThat(LOGFILE).exists();
        List<String> lines = Files.readAllLines(LOGFILE);
        String[] expectedLineEndings = {
                "INFO d.c.d.L.Foo [main] [doSomething()] Within doSomething() ...",
                "INFO d.c.d.L.Foo [main] [invokeBar()] Within invokeBar() ...",
                "INFO d.c.d.L.Bar [main] [throwSomething()] Within throwSomething() ...",
                "WARN d.c.d.Log4j2RouterUnit5 [main] [loggingRouter()] Catched: java.lang.RuntimeException: This is a test."
        };
        assertThat(lines).hasSizeGreaterThanOrEqualTo(expectedLineEndings.length);
        for (int i = 0; i < expectedLineEndings.length; i++) {
            assertThat(lines.get(i)).endsWith(expectedLineEndings[i]);
        }
    }
}
