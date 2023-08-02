package de.christofreichardt.diagnosis;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TracerFactoryUnit5 implements WithAssertions {
    public static final String PATH_TO_LOGDIR = "." + File.separator + "log";
    final private BannerPrinter bannerPrinter = new BannerPrinter();

    @BeforeAll
    void printHeader() {
        this.bannerPrinter.startUnit(getClass());
    }

    @BeforeEach
    void init() throws IOException {
        System.out.printf("%nResetting TracerFactory ...%n");
        TracerFactory.getInstance().reset();

        Path logDir = Path.of(PATH_TO_LOGDIR);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logDir, "*.log")) {
            for (Path path : stream) {
                System.out.printf("Deleting '%s' ...%n", path);
                Files.delete(path);
            }
        }
    }

    @Test
    void dummy() {
        this.bannerPrinter.start("dummy", getClass());
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
        assertThat(defaultTracer).isEqualTo(TracerFactory.NULLTRACER); // since we are resetting the TracerFactory instance before each test case
    }

    @Test
    void configuration() {
        this.bannerPrinter.start("configuration", getClass());

        Path config = Path.of(".", "config", "TraceConfig.xml");
    }
}
