/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

/**
 * Has been introduced to exchange thread maps which are based on <code>Map&lt;Thread,TracingContext&gt;</code> with 
 * thread maps which are based on <code>ThreadLocal</code>s and vice versa.
 * 
 * @author Christof Reichardt
 */
abstract public class AbstractThreadMap {
  
  /**
   * Indicates a corruption of the method stack.
   */
  static public class RuntimeException extends java.lang.RuntimeException {

    public RuntimeException(String msg) {
      super(msg);
    }

    public RuntimeException(Throwable cause) {
      super(cause);
    }
  }

  /** denotes the maximal number of traced methods on the stack */
  public static final int STACK_SIZE = 50;
  
  /**
   * Returns the stack size of the current thread. The value -1 indicates that the current thread isn't registered.
   * 
   * @return the current stack size or -1 if there is no stack for the current thread
   */
  abstract public int getCurrentStackSize();
  
  abstract TracingContext getCurrentTracingContext();
  
  abstract void setCurrentTracingContext(TracingContext tracingContext);
  
  abstract TracingContext removeCurrentTracingContext();
  
  abstract boolean push(TraceMethod traceMethod);
  
  abstract TraceMethod pop();
}
