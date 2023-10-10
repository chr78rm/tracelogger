package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.BannerPrinter;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.diagnosis.io.NullPrintStream;
import de.christofreichardt.diagnosis.io.TracePrintStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileTracerUnit5 implements WithAssertions {
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
    void invalidFileSize() {
        this.bannerPrinter.start("invalidFileSize", getClass());

        Path config = Path.of(".", "config", "InvalidTraceConfig_4.xml");
        assertThatExceptionOfType(TracerFactory.Exception.class).isThrownBy(
                        () -> TracerFactory.getInstance().readConfiguration(config.toFile())
                )
                .havingCause()
                .withMessageEndingWith("'1048NaN576' is not a valid value for 'integer'.");
    }

    @Test
    void minimumFileSizeLimitUndercut() {
        this.bannerPrinter.start("minimumFileSizeLimitUndercut", getClass());

        Path config = Path.of(".", "config", "InvalidTraceConfig_5.xml");
        assertThatExceptionOfType(TracerFactory.Exception.class).isThrownBy(
                        () -> TracerFactory.getInstance().readConfiguration(config.toFile())
                )
                .havingCause()
                .withMessageContaining("Value '4096' is not facet-valid with respect to minInclusive '65536'");
    }

    @Test
    void maximumFileSizeLimitExceeded() {
        this.bannerPrinter.start("maximumFileSizeLimitExceeded", getClass());

        Path config = Path.of(".", "config", "InvalidTraceConfig_6.xml");
        assertThatExceptionOfType(TracerFactory.Exception.class).isThrownBy(
                        () -> TracerFactory.getInstance().readConfiguration(config.toFile())
                )
                .havingCause()
                .withMessageContaining("Value '134217729' is not facet-valid with respect to maxInclusive '134217728'");
    }

    @Test
    void simpleFileTracer() {
        this.bannerPrinter.start("simpleFileTracer", getClass());

        Path path = Path.of(".", "log", "Test.log");
        FileTracer fileTracer = new FileTracer("Test");
        assertThat(fileTracer.isOpened()).isFalse();
        assertThat(Files.exists(path)).isFalse();
        fileTracer.open();
        assertThat(fileTracer.isOpened()).isTrue();
        fileTracer.close();
        assertThat(fileTracer.isOpened()).isFalse();
        assertThat(Files.exists(path) && Files.isRegularFile(path)).isTrue();
    }

    @Test
    void logFileRolling() throws IOException { // TODO: occasionally fails (perhaps due to file system quirks on windows)
        this.bannerPrinter.start("logFileRolling", getClass());

        final String TRACER_NAME = "Test";
        final FileTracer fileTracer = new FileTracer(TRACER_NAME);
        final long LIMIT = 8192;
        final int NUMBER_OF_ROLLS = 3;
        final int NUMBER_OF_CHARS = System.lineSeparator().length() == 2 ? 100 : 101;
        final char[] CHARS = new char[NUMBER_OF_CHARS];
        Arrays.fill(CHARS, '-');
        final String TESTLINE = new String(CHARS);
        final long CHARS_PER_LINE = String.format("%s%n", TESTLINE).length();
        System.out.printf("CHARS_PER_LINE = %d%n", CHARS_PER_LINE);
        final long LINES = LIMIT/CHARS_PER_LINE + 1;
        System.out.printf("LINES = %d%n", LINES);

        Files.createFile(Path.of(".", "log", String.format("%s.1.log", TRACER_NAME)));

        fileTracer.setByteLimit(LIMIT);
        try {
            fileTracer.open();
            fileTracer.initCurrentTracingContext(5, true);

            class Dummy {
                void method() {
                    for (int i = 0; i < LINES; i++) {
                        fileTracer.out().println(TESTLINE);
                        fileTracer.out().flush();
                    }
                }
            }

            for (int i = 0; i < NUMBER_OF_ROLLS; i++) {
                Dummy dummy = new Dummy();
                dummy.method();
            }
        } finally {
            fileTracer.close();
        }

        Path path = Path.of(".", "log", "Test.log");
        assertThat(Files.exists(path) && Files.isRegularFile(path)).isTrue();
        assertThat(path.toFile().length()).isGreaterThan(0L);
        for (int i=0; i<NUMBER_OF_ROLLS; i++) {
            path = Path.of(".", "log", String.format("Test.%d.log", i));
            assertThat(Files.exists(path) && Files.isRegularFile(path)).isTrue();
            assertThat(path.toFile().length()).isGreaterThanOrEqualTo(LIMIT);
        }
    }

    @Test
    void debugLevel() throws IOException {
        this.bannerPrinter.start("debugLevel", getClass());

        final String TRACERNAME = "SimpleTest";
        final FileTracer fileTracer = new FileTracer(TRACERNAME);
        try {
            fileTracer.open();

            class Dummy {
                void method_0() {
                    assertThat(fileTracer.out()).isInstanceOf(NullPrintStream.class);
                    fileTracer.entry("void", this, "method_0()");
                    try {
                        assertThat(fileTracer.out()).isInstanceOf(NullPrintStream.class);
                    }
                    finally {
                        fileTracer.wayout();
                        assertThat(fileTracer.out()).isInstanceOf(NullPrintStream.class);
                    }
                }
                void method_1() {
                    fileTracer.initCurrentTracingContext(2, true);
                    assertThat(fileTracer.out()).isInstanceOf(TracePrintStream.class);
                    fileTracer.entry("void", this, "method_1()");
                    try {
                        assertThat(fileTracer.out()).isInstanceOf(TracePrintStream.class);
                        method_2();
                    }
                    finally {
                        fileTracer.wayout();
                        assertThat(fileTracer.out()).isInstanceOf(TracePrintStream.class);
                    }
                }
                void method_2() {
                    assertThat(fileTracer.out()).isInstanceOf(TracePrintStream.class);
                    fileTracer.entry("void", this, "method_2()");
                    try {
                        assertThat(fileTracer.out()).isInstanceOf(TracePrintStream.class);
                        method_3();
                    }
                    finally {
                        fileTracer.wayout();
                        assertThat(fileTracer.out()).isInstanceOf(TracePrintStream.class);
                    }
                }
                void method_3() {
                    assertThat(fileTracer.out()).isInstanceOf(TracePrintStream.class);
                    fileTracer.entry("void", this, "method_3()");
                    try {
                        assertThat(fileTracer.out()).isInstanceOf(NullPrintStream.class);
                        fileTracer.out().printfIndentln("This is a test.");
                    }
                    finally {
                        fileTracer.wayout();
                        assertThat(fileTracer.out()).isInstanceOf(TracePrintStream.class);
                    }
                }
            }

            Dummy dummy = new Dummy();
            dummy.method_0();
            dummy.method_1();
        }
        finally {
            fileTracer.close();
        }

        Path traceLogPath = Path.of(".", "log", String.format("%s.log", TRACERNAME));
        assertThat(traceLogPath).exists();
        Pattern pattern = Pattern.compile("void Dummy\\[[0-9]+\\]\\.method_[123]\\(\\)");
        try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(traceLogPath.toFile()))) {
            String line;
            while ((line = lineNumberReader.readLine()) != null) {
//                System.out.printf("line[%d] = %s%n", lineNumberReader.getLineNumber(), line);
                if (lineNumberReader.getLineNumber() >= 6  &&  lineNumberReader.getLineNumber() <= 11) {
                    Matcher matcher = pattern.matcher(line);
                    assertThat(matcher.find()).isTrue();
                }
            }
        }
    }

    @Test
    void example() {
        this.bannerPrinter.start("example", getClass());

        final AbstractTracer tracer = new FileTracer("Example");
        try {
            tracer.open();
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
            foo.bar(); // nothing will be printed because no tracing context is provided
            tracer.initCurrentTracingContext(2, true);
            foo.bar(); // this will generate output
        }
        finally {
            tracer.close();
        }
        Path path = Path.of(".", "log", "Example.log");
        assertThat(Files.exists(path) && Files.isRegularFile(path)).isTrue();
    }}
