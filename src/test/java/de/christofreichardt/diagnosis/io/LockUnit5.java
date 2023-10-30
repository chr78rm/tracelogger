package de.christofreichardt.diagnosis.io;

import de.christofreichardt.diagnosis.BannerPrinter;
import de.christofreichardt.diagnosis.file.FileTracer;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LockUnit5 implements WithAssertions {
    public static final Path LOGDIR = Path.of(".", "log", "lock");
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

    @Test
    void simpleWriteLock() {
        this.bannerPrinter.start("simpleWriteLock", getClass());

        FileTracer fileTracer = new FileTracer("Test");
        fileTracer.setLogDirPath(LOGDIR);
        fileTracer.open();
        try {
            fileTracer.initCurrentTracingContext(1, true);
            fileTracer.entry("void", this, "simpleWriteLock()");
            try {
                fileTracer.out().printfIndentln("This is a test.");
                assertThat(fileTracer.out()).isInstanceOf(TracePrintStream.class);
                TracePrintStream tracePrintStream = (TracePrintStream) fileTracer.out();
                assertThat(tracePrintStream.lock.isLocked()).isFalse();
                assertThat(tracePrintStream.lock.isHeldByCurrentThread()).isFalse();
                assertThat(tracePrintStream.lock.getHoldCount()).isEqualTo(0);
                fileTracer.out().lock();
                try {
                    assertThat(tracePrintStream.lock.isLocked()).isTrue();
                    assertThat(tracePrintStream.lock.isHeldByCurrentThread()).isTrue();
                    assertThat(tracePrintStream.lock.getHoldCount()).isEqualTo(1);
                } finally {
                    fileTracer.out().unlock();
                }
                assertThat(tracePrintStream.lock.isLocked()).isFalse();
                assertThat(tracePrintStream.lock.isHeldByCurrentThread()).isFalse();
                assertThat(tracePrintStream.lock.getHoldCount()).isEqualTo(0);
                fileTracer.entry("void", this, "simpleWriteLock()");
                try {
                    assertThat(fileTracer.out()).isInstanceOf(NullPrintStream.class);
                    fileTracer.out().lock();
                    try {
                        assertThat(tracePrintStream.lock.isLocked()).isFalse();
                        assertThat(tracePrintStream.lock.isHeldByCurrentThread()).isFalse();
                        assertThat(tracePrintStream.lock.getHoldCount()).isEqualTo(0);
                    } finally {
                        fileTracer.out().unlock();
                    }
                } finally {
                    fileTracer.wayout();
                }
            } finally {
                fileTracer.wayout();
            }
        } finally {
            fileTracer.close();
        }
    }

    @Test
    void runWithLock() {
        this.bannerPrinter.start("runWithLock", getClass());

        FileTracer fileTracer = new FileTracer("Test");
        fileTracer.setLogDirPath(LOGDIR);
        fileTracer.open();
        try {
            fileTracer.initCurrentTracingContext(1, true);
            assertThat(fileTracer.out()).isInstanceOf(TracePrintStream.class);
            final TracePrintStream tracePrintStream = (TracePrintStream) fileTracer.out();
            fileTracer.entry("void", this, "simpleWriteLock()");
            try {
                fileTracer.out().runWithLock(() -> {
                    assertThat(tracePrintStream.lock.isLocked()).isTrue();
                    fileTracer.out().printfIndentln("This is a test.");
                });
            } finally {
                fileTracer.wayout();
            }
        } finally {
            fileTracer.close();
        }
    }
}
