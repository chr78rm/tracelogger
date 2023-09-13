package de.christofreichardt.diagnosis;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PerformanceUnit5 {

    @BeforeAll
    void printHeader() {
        final BannerPrinter bannerPrinter = new BannerPrinter();
        bannerPrinter.startUnit(getClass());
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void triangularNumberWithNullTracer() {
        System.out.printf("%nResetting TracerFactory ...%n");
        TracerFactory.getInstance().reset();
        NullTracer nullTracer = TracerFactory.getInstance().getDefaultTracer();
        BigInteger sum = new BigInteger("0");
        final long LIMIT = 1000*1000*1000;
        for (long i=0; i<LIMIT; i++) {
            sum = sum.add(BigInteger.valueOf(i));
            nullTracer.out().printfIndentln("sum = %d", sum);
        }
        System.out.printf("sum = %d%n", sum);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void triangularNumberWithoutTracing() {
        BigInteger sum = new BigInteger("0");
        final long LIMIT = 1000*1000*1000;
        for (long i=0; i<LIMIT; i++) {
            sum = sum.add(BigInteger.valueOf(i));
        }
        System.out.printf("%nsum = %d%n", sum);
    }

    @Test
    @Disabled
    void runBenchmarks() throws RunnerException {
        final BannerPrinter bannerPrinter = new BannerPrinter();
        bannerPrinter.start("runBenchmarks", getClass());

        Options options = new OptionsBuilder()
                .include(this.getClass().getSimpleName())
                .mode(Mode.AverageTime)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(6)
                .threads(1)
                .measurementIterations(6)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();
        Runner runner = new Runner(options);
        runner.run();
    }
}
