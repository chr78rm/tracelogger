package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.BannerPrinter;
import de.christofreichardt.diagnosis.JDKLoggingRouter;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.NullTracer;
import de.christofreichardt.diagnosis.io.NullPrintStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JDKLoggerUnit5 implements WithAssertions {
    public static final Path LOGDIR = Path.of(".", "log", "jdk14");
    public static final Path LOGFILE = LOGDIR.resolve("jdk14-test.0.log");
    final private BannerPrinter bannerPrinter = new BannerPrinter();

    @BeforeAll
    void printHeader() {
        this.bannerPrinter.startUnit(getClass());
    }

    @BeforeEach
    void init() throws IOException {
        System.out.printf("%nResetting JDK14 LogManager ...%n");
        LogManager.getLogManager().reset();

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
    void jdk14Config() throws IOException {
        this.bannerPrinter.start("jdk14Config", getClass());

        Path config = Path.of(".", "config", "logging.properties");
        try (FileInputStream inputStream = new FileInputStream(config.toFile())) {
            LogManager.getLogManager().readConfiguration(inputStream);
        }
        Logger.getLogger(getClass().getName()).info("This is a test.");
        assertThat(LOGFILE).exists();
    }

    @Test
    void loggingRouter() throws IOException {
        this.bannerPrinter.start("loggingRouter", getClass());

        Path config = Path.of(".", "config", "logging.properties");
        try (FileInputStream inputStream = new FileInputStream(config.toFile())) {
            LogManager.getLogManager().readConfiguration(inputStream);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        class Dummy {
            final NullTracer tracer;

            public Dummy(NullTracer tracer) {
                this.tracer = tracer;
            }

            void method_0() {
                assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
                tracer.entry("void", this, "method_0()");
                tracer.wayout();
                assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
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
                    tracer.logMessage(LogLevel.INFO, "Within method_2.", getClass(), "method_1()");
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
                    tracer.logMessage(LogLevel.INFO, "Within method_3.", getClass(), "method_1()");
                    tracer.out().printfIndentln("This output goes to /dev/null.");
                    throw new java.lang.RuntimeException("This is a test.");
                } finally {
                    tracer.wayout();
                    assertThat(tracer.out()).isInstanceOf(NullPrintStream.class);
                }
            }
        }

        System.out.printf("Default charset: %s%n", System.getProperty("file.encoding"));

        JDKLoggingRouter jdkLoggingRouter = new JDKLoggingRouter();
        jdkLoggingRouter.initCurrentTracingContext(2, true); // must not have any effect since it's a NullTracer
        Dummy dummy = new Dummy(jdkLoggingRouter);
        dummy.method_0();
        try {
            dummy.method_1();
        } catch (Exception ex) {
            jdkLoggingRouter.logException(LogLevel.WARNING, ex, getClass(), "loggingRouter()");
        }

        assertThat(LOGFILE).exists();
        List<String> lines = Files.readAllLines(LOGFILE);
        Pattern pattern = Pattern.compile("\\[[A-Za-z0-9: ]+(\\d){4}]");
        assertThat(
                lines.stream()
                        .filter(line -> pattern.matcher(line).find())
                        .count()
        ).isEqualTo(4);
        String[] expectedStrings = {"Within method_1.", "Within method_2.", "Within method_3.", "This is a test."};
        for (int i=0; i<4; i++) {
            assertThat(lines.get(i)).contains(expectedStrings[i]);
        }
        assertThat(
                lines.stream()
                        .filter(line -> line.equals("java.lang.RuntimeException: This is a test."))
                        .count()
        ).isEqualTo(1);
    }
}
