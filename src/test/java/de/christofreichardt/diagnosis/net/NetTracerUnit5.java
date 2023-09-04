package de.christofreichardt.diagnosis.net;

import de.christofreichardt.diagnosis.BannerPrinter;
import de.christofreichardt.diagnosis.TracerFactory;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.*;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NetTracerUnit5 implements WithAssertions {
    final private BannerPrinter bannerPrinter = new BannerPrinter();

    @BeforeAll
    void printHeader() {
        this.bannerPrinter.startUnit(getClass());
    }

    @BeforeEach
    void init() {
        System.out.printf("%nResetting TracerFactory ...%n");
        TracerFactory.getInstance().reset();
    }

    @Test
    void simpleNetTracer() throws InterruptedException, ExecutionException, TimeoutException {
        this.bannerPrinter.start("simpleNetTracer", getClass());

        CountDownLatch countDownLatch = new CountDownLatch(1);
        final int SOCKET_TIMEOUT = 5000, RECEIVER_TIMEOUT = 2500, SERVICE_TIMEOUT = 2500;

        class Receiver implements Callable<Boolean> {
            int portNo = -1;

            @Override
            public Boolean call() throws java.lang.Exception {
                try (ServerSocket listener = new ServerSocket(0)) {
                    portNo = listener.getLocalPort();
                    listener.setSoTimeout(SOCKET_TIMEOUT);
                    countDownLatch.countDown();
                    try (Socket socket = listener.accept()) {
                        LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
                        String line;
                        do {
                            line = lineNumberReader.readLine();
//                            System.out.printf("line = %s%n", line);
                            if (lineNumberReader.getLineNumber() == 0) {
                                String[] splits = line.split(",");
                                if (!(splits.length == 3)) {
                                    throw new java.lang.Exception("Expected three credentials.");
                                }
                            }
                        } while (line != null);
                    }
                }

                return true;
            }
        }

        NetTracer netTracer = new NetTracer("Test");
        netTracer.setHostName("localhost");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Receiver receiver = new Receiver();
            Future<Boolean> future = executorService.submit(receiver);
            boolean isReceiverUp = countDownLatch.await(RECEIVER_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!isReceiverUp) {
                throw new RuntimeException("No receiver.");
            }
            netTracer.setPortNo(receiver.portNo);
            try {
                netTracer.open();
            } finally {
                netTracer.close();
            }
            assertThat(future.get(RECEIVER_TIMEOUT, TimeUnit.MILLISECONDS)).isTrue();
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
    void invalidPortNumber() {
        this.bannerPrinter.start("invalidPortNumber", getClass());

        Path config = Path.of(".", "config", "InvalidTraceConfig_7.xml");
        assertThatExceptionOfType(TracerFactory.Exception.class).isThrownBy(
                        () -> TracerFactory.getInstance().readConfiguration(config.toFile())
                )
                .havingCause()
                .withCauseInstanceOf(org.xml.sax.SAXException.class)
                .withMessageContaining("Value '65536' is not facet-valid with respect to maxInclusive '65535' for type 'unsignedShort'");
    }
}
