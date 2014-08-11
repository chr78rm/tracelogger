/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis.net;

import de.christofreichardt.diagnosis.BannerPrinter;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christof Reichardt
 */
public class NetTracerUnit {
  final private BannerPrinter bannerPrinter = new BannerPrinter();
  
  @Before
  public void setUp() {
    
  }  
  
  @After
  public void tearDown() {
  }
  
  /**
   * Creates a NetTracer manually and tries to connect to a receiver on localhost.
   * 
   * @throws java.lang.InterruptedException
   * @throws java.util.concurrent.ExecutionException
   * @throws java.util.concurrent.TimeoutException
   */
  @Test
  public void simpleNetTracer() throws InterruptedException, ExecutionException, TimeoutException {
    this.bannerPrinter.start("simpleNetTracer", getClass());
    
    final int TIME_OUT = 10;
    final int PORT_NO = 5010;
    NetTracer netTracer = new NetTracer("Test");
    netTracer.setHostName("localhost");
    netTracer.setPortNo(PORT_NO);
    
    class Receiver implements Callable<Boolean> {
      @Override
      public Boolean call() throws java.lang.Exception {
        try (ServerSocket listener = new ServerSocket(PORT_NO)) {
          try (Socket socket = listener.accept();) {
            LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
            String line;
            do {
              line = lineNumberReader.readLine();
              if (lineNumberReader.getLineNumber() == 0) {
                String[] splits = line.split(",");
                if (!(splits.length == 3))
                  throw new java.lang.Exception("Expected three credentials.");
              }
            } while (line != null);
          }
        }
        
        return true;
      }
    }
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    try {
      Receiver receiver = new Receiver();
      Future<Boolean> future = executorService.submit(receiver);
      Thread.sleep(2500);
      try {
        netTracer.open();
      }
      finally {
        netTracer.close();
      }
      Assert.assertTrue(future.get(TIME_OUT, TimeUnit.SECONDS));
    }
    finally {
      executorService.shutdown();
    }
  }
}
