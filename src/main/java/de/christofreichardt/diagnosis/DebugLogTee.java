/*
 * Copyright 2014-2023 Christof Reichardt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.io.IndentablePrintStream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 * An abstract base class which provides the environment to connect additionally to another log system, such as log4j. 
 * This class honors the contract of the {@link AbstractTracer} base class but does so by wrapping and delegating to another {@link AbstractTracer}.
 * Besides two abstract adapt methods must be implemented to route log messages to the desired log system. Note that the wrapped tracer instance must 
 * not be a {@link NullTracer}. An adapter which is only interested in logging messages and wants to discard the additional tracing information altogether 
 * should be derived directly from the {@link NullTracer}.
 * 
 * @author Christof Reichardt
 * @param <T> The actual tracer type
 */
abstract public class DebugLogTee<T extends AbstractTracer> extends AbstractTracer {
  
  /** Some tracer. */
  final protected T tracer;

  /**
   * Constructor expects the name of the tracer and the to be wrapped Tracer instance.
   * @param name the name of the tracer
   * @param tracer the to be wrapped tracer (must not be a NullTracer instance)
   */
  public DebugLogTee(String name, T tracer) {
    super(name);
    if (NullTracer.class.isAssignableFrom(tracer.getClass()))
      throw new IllegalArgumentException("NullTracers aren't allowed.");
    this.tracer = tracer;
  }

  @Override
  final public int getBufferSize() {
    return this.tracer.getBufferSize();
  }

  @Override
  final public boolean isAutoflush() {
    return this.tracer.isAutoflush();
  }

  @Override
  final public boolean isOpened() {
    return this.tracer.isOpened();
  }

  @Override
  final public String getName() {
    return super.getName();
  }

  @Override
  final public void setBufferSize(int bufferSize) {
    this.tracer.setBufferSize(bufferSize);
  }

  @Override
  final public void setAutoflush(boolean autoflush) {
    this.tracer.setAutoflush(autoflush);
  }

  @Override
  final protected void readConfiguration(XPath xpath, Node node) throws XPathExpressionException, AbstractTracer.Exception {
    this.tracer.readConfiguration(xpath, node);
  }

  @Override
  public void open() {
    this.tracer.open();
  }

  @Override
  public void close() {
    this.tracer.close();
  }

  @Override
  final public void initCurrentTracingContext() {
    this.tracer.initCurrentTracingContext();
  }

  @Override
  final public void initCurrentTracingContext(int debugLevel, boolean online) {
    this.tracer.initCurrentTracingContext(debugLevel, online);
  }
  
  /**
   * Derived classes should provide code which routes the given message with the denoted logLevel and class to the desired log system.
   * 
   * @param logLevel the to be translated LogLevel
   * @param message the actual message
   * @param clazz the class context of the message
   */
  abstract protected void adapt(LogLevel logLevel, String message, Class<?> clazz);
  
  /**
   * Derived classes should provide code which routes the given throwable with the denoted logLevel and class to the desired log system.
   * @param logLevel the to be translated LogLevel
   * @param throwable references the to be logged info
   * @param clazz the class context of the message
   */
  abstract protected void adapt(LogLevel logLevel, Throwable throwable, Class<?> clazz);

  @Override
  public TraceMethod entry(String returnType, Class<?> clazz, String methodSignature) {
    return this.tracer.entry(returnType, clazz, methodSignature);
  }

  @Override
  public TraceMethod entry(String returnType, Object object, String methodSignature) {
    return this.tracer.entry(returnType, object, methodSignature);
  }

  @Deprecated
  @Override
  public TraceMethod entry(String methodSignature) {
    return this.tracer.entry(methodSignature);
  }

  @Override
  final public void logMessage(LogLevel logLevel, String message, Class<?> clazz, String methodName) {
    this.tracer.logMessage(logLevel, message, clazz, methodName);
    adapt(logLevel, message, clazz);
  }

  @Override
  final public void logException(LogLevel logLevel, Throwable throwable, Class<?> clazz, String methodName) {
    this.tracer.logException(logLevel, throwable, clazz, methodName);
    adapt(logLevel, throwable, clazz);
  }

  @Override
  public TraceMethod wayout() {
    return this.tracer.wayout();
  }

  @Override
  final public IndentablePrintStream out() {
    return this.tracer.out();
  }
  
}
