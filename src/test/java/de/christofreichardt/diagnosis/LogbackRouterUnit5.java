package de.christofreichardt.diagnosis;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import de.christofreichardt.diagnosis.io.NullPrintStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.LoggerFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LogbackRouterUnit5 implements WithAssertions {
    public static final Path LOGDIR = Path.of(".", "log", "logback");
    public static final Path LOGFILE = LOGDIR.resolve("logback-test.log");
    final private BannerPrinter bannerPrinter = new BannerPrinter();

    @BeforeAll
    void printHeader() {
        this.bannerPrinter.startUnit(getClass());
    }

    @BeforeEach
    void init() throws IOException, JoranException {
        final Path EMPTY_LOG = LOGDIR.resolve("empty.log");
        DirectoryStream.Filter<Path> filter = path -> (path.getFileName().toString().endsWith(".log") && !path.equals(EMPTY_LOG));
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(LOGDIR, filter)) {
            for (Path path : stream) {
                System.out.printf("Deleting '%s' ...%n", path);
                Files.delete(path);
            }
        }

        System.out.printf("%nResetting LoggerContext ...%n");
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        File configFile = new File("." + File.separator + "config" + File.separator + "logback.xml");
        configurator.doConfigure(configFile);
        StatusPrinter.print(context);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static class Dummy implements WithAssertions {
        final NullTracer tracer;

        public Dummy(NullTracer tracer) {
            this.tracer = tracer;
        }

        void method_0() {
            assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
            tracer.entry("void", this, "method_0()");
            try {
                tracer.logMessage(LogLevel.ERROR, "Within method_0. This is a test.", getClass(), "method_3()");
            } finally {
                tracer.wayout();
                assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
            }
        }

        void method_1() {
            assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
            tracer.entry("void", this, "method_1()");
            try {
                assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
                tracer.logMessage(LogLevel.INFO, "Within method_1.", getClass(), "method_1()");
                method_2();
            } finally {
                tracer.wayout();
                assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
            }
        }

        void method_2() {
            assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
            tracer.entry("void", this, "method_2()");
            try {
                assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
                tracer.logMessage(LogLevel.INFO, "Within method_2.", getClass(), "method_2()");
                method_3();
            } finally {
                tracer.wayout();
                assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
            }
        }

        void method_3() {
            assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
            tracer.entry("void", this, "method_3()");
            try {
                assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
                tracer.logMessage(LogLevel.INFO, "Within method_3.", getClass(), "method_3()");
                tracer.out().printfIndentln("This output goes to /dev/null.");
                throw new java.lang.RuntimeException("This is a test.");
            } finally {
                tracer.wayout();
                assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
            }
        }
    }

    @Test
    void loggingRouter() throws IOException {
        this.bannerPrinter.start("loggingRouter", getClass());

        LogbackRouter logbackRouter = new LogbackRouter();
        Dummy dummy = new Dummy(logbackRouter);
        dummy.method_0();
        try {
            dummy.method_1();
        } catch (Exception ex) {
            logbackRouter.logException(LogLevel.WARNING, ex, getClass(), "loggingRouter()");
        }
        assertThat(LOGFILE).exists();
        String[] expectedLineEndings = {
                "[main] ERROR d.c.d.LogbackRouterUnit5$Dummy method_0 - Within method_0. This is a test.",
                "[main] INFO  d.c.d.LogbackRouterUnit5$Dummy method_1 - Within method_1.",
                "[main] INFO  d.c.d.LogbackRouterUnit5$Dummy method_2 - Within method_2.",
                "[main] INFO  d.c.d.LogbackRouterUnit5$Dummy method_3 - Within method_3.",
                "[main] WARN  d.c.diagnosis.LogbackRouterUnit5 loggingRouter - This is a test.",
                "java.lang.RuntimeException: This is a test."
        };
        List<String> lines = Files.readAllLines(LOGFILE);
        assertThat(lines).hasSizeGreaterThanOrEqualTo(expectedLineEndings.length);
        for (int i = 0; i < expectedLineEndings.length; i++) {
            assertThat(lines.get(i)).endsWith(expectedLineEndings[i]);
        }
    }
}
