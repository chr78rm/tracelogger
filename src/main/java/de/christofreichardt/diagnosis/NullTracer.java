/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.io.NullPrintStream;
import javax.xml.xpath.XPath;
import org.w3c.dom.Node;

/**
 * Instances of this tracer type don't trace anything and therefore need not to manage any method stacks. This fast tracer may be used
 * at production time. Derived classes should be made final and may override the logException()- and logMessage()-methods by connecting to an
 * alternative logging system.
 * 
 * @author  Christof Reichardt
 */
public class NullTracer extends AbstractTracer {

  /**
   * Default constructor.
   */
  public NullTracer() {
    super("__NullTracer__");
  }

  /**
   * Constructor for pooled tracers with (unique) names.
   * @param name the name of the tracer
   */
  public NullTracer(String name) {
    super(name);
  }
  
 /**
   * Pseudo readConfiguration()-method.
   * 
   * @param xpath (ignored)
   * @param node (ignored)
   */
  @Override
  protected void readConfiguration(XPath xpath, Node node) {
  }

  /**
   * Pseudo open()-method.
   */
  @Override
  final public void open() {
    super.setOpened(true);
  }

  /**
   * Pseudo close()-method.
   */
  @Override
  final public void close() {
    super.setOpened(false);
  }

  /**
   * Returns always a {@link NullPrintStream} regardless of the given level.
   * 
   * @param level (ignored)
   * @return always a NullPrintStream
   */
  @Override
  final protected NullPrintStream out(int level) {
    return this.getNullPrintStream();
  }

  /**
   * Returns always a {@link NullPrintStream}.
   * 
   * @return a NullPrintStream
   */
  @Override
  final public NullPrintStream out() {
    return this.getNullPrintStream();
  }

  /**
   * Pseudo entry()-method.
   * 
   * @param methodSignature (ignored)
   * @return always null
   * @deprecated provided for backwards compatibility
   */
  @Deprecated
  @Override
  final public TraceMethod entry(String methodSignature) {
    return null;
  }

  /**
   * Pseudo entry()-method.
   * 
   * @param returnType (ignored)
   * @param object (ignored)
   * @param methodSignature (ignored)
   * @return always null
   */
  @Override
  final public TraceMethod entry(String returnType, Object object, String methodSignature) {
    return null;
  }

  /**
   * Pseudo entry()-method.
   * 
   * @param returnType (ignored)
   * @param clazz (ignored)
   * @param methodSignature (ignored)
   * @return always null
   */
  @Override
  final public TraceMethod entry(String returnType, Class<?> clazz, String methodSignature) {
    return null;
  }

//  /**
//   * Pseudo printMethodEntry()-method.
//   * @param methodSignature (ignored)
//   */
//  @Override
//  protected void printMethodEntry(String methodSignature) {
//  }
  
  /**
   * Pseudo wayout()-method.
   * @return always null
   */
  @Override
  final public TraceMethod wayout() {
    return null;
  }

  /**
   * Pseudo initCurrentTracingContext()-method.
   */
  @Override
  final public void initCurrentTracingContext() {
  }

  /**
   * Pseudo initCurrentTracingContext()-method.
   * 
   * @param debugLevel (ignored)
   * @param online (ignored)
   */
  @Override
  final public void initCurrentTracingContext(int debugLevel, boolean online) {
  }

  /**
   * Pseudo logException()-method. Derived classes should provide code which connects to an alternative logging system.
   * @param logLevel (ignored)
   * @param throwable (ignored)
   * @param clazz (ignored)
   */
  @Override
  public void logException(LogLevel logLevel, Throwable throwable, Class<?> clazz, String methodName) {
  }

  /**
   * Pseudo logMessage()-method. Derived classes should provide code which connects to an alternative logging system.
   * @param logLevel (ignored)
   * @param message (ignored)
   * @param clazz (ignored) 
   */
  @Override
  public void logMessage(LogLevel logLevel, String message, Class<?> clazz, String methodName) {
  }
}
