/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.diagnosis;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class maps threads on {@link TracingContext}s. It provides methods to set, access and remove the {@link TracingContext}
 * of the current thread. Furthermore to be monitored methods can be pushed on or popped from the method stack of the current
 * {@link TracingContext}. For internal use primarily.
 * 
 * @author Christof Reichardt
 */
public class ThreadMap extends AbstractThreadMap {

//  /**
//   * Indicates a corruption of the method stack.
//   */
//  static public class RuntimeException extends java.lang.RuntimeException {
//
//    public RuntimeException(String msg) {
//      super(msg);
//    }
//
//    public RuntimeException(Throwable cause) {
//      super(cause);
//    }
//  }
//
//  /** denotes the maximal number of traced methods on the stack */
//  public static final int STACK_SIZE = 50;
  
  /** maps the thread context on thread names */
  private final ConcurrentMap<Thread, TracingContext> tracingContextMap = new ConcurrentHashMap<>();

  /**
   * Returns the stack size of the current thread. The value -1 indicates that the current thread isn't registered.
   * 
   * @return the current stack size
   */
  @Override
  public int getCurrentStackSize() {
    int stackSize = -1;
    if (this.tracingContextMap.containsKey(Thread.currentThread()))
      stackSize = this.tracingContextMap.get(Thread.currentThread()).getMethodStack().size();
    
    return stackSize;
  }

  @Override
  TracingContext getCurrentTracingContext() { // TODO: rewrite this using Map.get()
    TracingContext tracingContext = null;
    if (this.tracingContextMap.containsKey(Thread.currentThread()))
      tracingContext = this.tracingContextMap.get(Thread.currentThread());
    
    return tracingContext;
  }
  
  @Override
  void setCurrentTracingContext(TracingContext tracingContext) {
    this.tracingContextMap.put(Thread.currentThread(), tracingContext);
  }
  
  @Override
  TracingContext removeCurrentTracingContext() {
    return this.tracingContextMap.remove(Thread.currentThread());
  }

  /**
   * Pushs a method onto the stack of the current thread.
   * 
   * @param traceMethod the method to be pushed
   */
  @Override
  boolean push(TraceMethod traceMethod) {
    boolean success;
    TracingContext currentTracingContext = getCurrentTracingContext();
    if (currentTracingContext != null && !currentTracingContext.isCorrupted()) {
      if (currentTracingContext.getMethodStack().size() >= STACK_SIZE) {
        currentTracingContext.setCorrupted(true);
        throw new ThreadMap.RuntimeException("Stacksize is exceeded.");
      }
      else {
        currentTracingContext.getMethodStack().push(traceMethod);
        success = true;
      }
    }
    else
      success = false;
    
    return success;
  }
  
  /**
   * Pops a method from the stack of the current thread.
   * 
   * @return the popped method
   */
  @Override
  TraceMethod pop() {
    TraceMethod traceMethod = null;
    TracingContext currentTracingContext = getCurrentTracingContext();
    if (currentTracingContext != null && !currentTracingContext.isCorrupted()) {
      try {
        traceMethod = currentTracingContext.getMethodStack().pop();
        traceMethod.stopTime();
      }
      catch (NoSuchElementException ex)
      {
        currentTracingContext.setCorrupted(true);
        throw new ThreadMap.RuntimeException(ex);
      }
    }

    return traceMethod;
  }
}
