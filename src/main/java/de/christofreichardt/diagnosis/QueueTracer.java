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
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 * <div style="text-align: justify">
 * A special tracer intended for the use by a bounded blocking queue. This is an option to retrieve tracer within a multi-threaded environment
 * when we cannot control the creation of the threads. An example would be applications within a container that manages a thread pool. This tracer
 * honors the contract of an {@link AbstractTracer} but does so by wrapping and delegating to another {@link AbstractTracer}. In particular the
 * current tracing context will be automatically cleared when calling {@link #wayout()} and the method stack remains empty afterwards. Thus the tracer
 * can be reused for another thread's tracing context without any danger to create a memory leak.
 * </div>
 *
 * @param <T> the wrapped tracer
 * @author Christof Reichardt
 */
abstract public class QueueTracer<T extends AbstractTracer> extends AbstractTracer {

    private boolean online;
    private int level;

    /**
     * Some tracer.
     */
    final protected T tracer;

    /**
     * Constructor expects the tracer name and the to be wrapped tracer instance.
     *
     * @param name   the name of the tracer
     * @param tracer the to be wrapped tracer
     */
    public QueueTracer(String name, T tracer) {
        super(name);
        this.tracer = tracer;
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     *
     * @return the buffer size of the wrapped tracer
     */
    @Override
    public int getBufferSize() {
        return this.tracer.getBufferSize();
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     *
     * @return indicates the autoflush mode of the wrapped tracer
     */
    @Override
    public boolean isAutoflush() {
        return this.tracer.isAutoflush();
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     *
     * @return indicates if the wrapped tracer is openend
     */
    @Override
    public boolean isOpened() {
        return this.tracer.isOpened();
    }

//  /**
//   * 
//   * @return 
//   */
//  @Override
//  public String getName() {
//    return this.tracer.getName();
//  }

    /**
     * Sets the buffer size of the wrapped tracer.
     *
     * @param bufferSize the bufferSize to set
     */
    @Override
    public void setBufferSize(int bufferSize) {
        this.tracer.setBufferSize(bufferSize);
    }

    /**
     * Sets the autoflush mode of the wrapped tracer
     *
     * @param autoflush the autoflush to set
     */
    @Override
    protected void setAutoflush(boolean autoflush) {
        this.tracer.setAutoflush(autoflush);
    }

    /**
     * Returns the configured {@code online} value. Will be used by the parameterless {@link QueueTracer#initCurrentTracingContext()} method.
     *
     * @return the configured {@code online} value
     */
    public boolean isOnline() {
        return this.online;
    }

    /**
     * Returns the configured debuglevel. Will be used by the parameterless {@link QueueTracer#initCurrentTracingContext()} method.
     *
     * @return the configured debug level
     */
    public int getLevel() {
        return this.level;
    }

    @Override
    protected void readConfiguration(XPath xpath, Node node) throws XPathExpressionException, AbstractTracer.Exception {
        this.online = Boolean.parseBoolean(
                (String) xpath.evaluate("./dns:Online/text()", node, XPathConstants.STRING)
        );
        this.level = Integer.parseInt(
                ((String) xpath.evaluate("./dns:DebugLevel/text()", node, XPathConstants.STRING)).strip()
        );
        setAutoflush(
                Boolean.parseBoolean((String) xpath.evaluate("./dns:TraceLogger/dns:AutoFlush/text()", node, XPathConstants.STRING))
        );
        setBufferSize(
                Integer.parseInt(
                        ((String) xpath.evaluate("./dns:TraceLogger/dns:BufSize/text()", node, XPathConstants.STRING)).strip()
                )
        );
    }

    /**
     * Opens the wrapped tracer.
     */
    @Override
    public void open() {
        this.tracer.open();
    }

    /**
     * Closes the wrapped tracer.
     */
    @Override
    public void close() {
        this.tracer.close();
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer by using the config parameter global
     * to all queued tracer.
     */
    @Override
    public void initCurrentTracingContext() {
        this.tracer.initCurrentTracingContext(this.level, this.online);
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     *
     * @param debugLevel controls the extent of the output
     * @param online     a value of false delivers no output of the current thread at all whereas a value of true delivers output controlled by debugLevel
     */
    @Override
    public void initCurrentTracingContext(int debugLevel, boolean online) {
        this.tracer.initCurrentTracingContext(debugLevel, online);
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     *
     * @param returnType      the return type of the method as string representation
     * @param clazz           the class to which that method belong
     * @param methodSignature the remaining method signature (without return type) inclusive parameter as string representation
     * @return the TraceMethod which has been put onto the stack - a mere data object for internal use primarily. May be null.
     */
    @Override
    public TraceMethod entry(String returnType, Class<?> clazz, String methodSignature) {
        return this.tracer.entry(returnType, clazz, methodSignature);
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     *
     * @param returnType      the return type of the method as string representation
     * @param object          the object that owns the method
     * @param methodSignature the remaining method signature (without return type) inclusive parameter as string representation
     * @return the TraceMethod which has been put onto the stack - a mere data object for internal use primarily. May be null.
     */
    @Override
    public TraceMethod entry(String returnType, Object object, String methodSignature) {
        return this.tracer.entry(returnType, object, methodSignature);
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     *
     * @param logLevel one of the predefined levels INFO, WARNING, ERROR, FATAL and SEVERE
     * @param message  the to be logged message
     * @param clazz    the originating class
     */
    @Override
    public void logMessage(LogLevel logLevel, String message, Class<?> clazz, String methodName) {
        this.tracer.logMessage(logLevel, message, clazz, methodName);
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     *
     * @param logLevel   one of the predefined levels INFO, WARNING, ERROR, FATAL and SEVERE
     * @param throwable  the to be logged throwable
     * @param clazz      the originating class
     * @param methodName the name of the relevant method
     */
    @Override
    public void logException(LogLevel logLevel, Throwable throwable, Class<?> clazz, String methodName) {
        this.tracer.logException(logLevel, throwable, clazz, methodName);
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     *
     * @return the thread map of the wrapped tracer
     */
    @Override
    protected AbstractThreadMap getThreadMap() {
        return this.tracer.getThreadMap();
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer. Besides it checks if the
     * stack size of the current tracing context has decreased to zero. If so then the current tracing context
     * will be cleared.
     *
     * @return the TraceMethod which has been popped from the stack - a mere data object for internal use primarily. May be null.
     */
    @Override
    public TraceMethod wayout() {
        TraceMethod traceMethod = this.tracer.wayout();
        if (getThreadMap().getCurrentStackSize() == 0) {
            clearCurrentTracingContext();
            if (!TracerFactory.getInstance().offerTracer(this)) {
                // this dubious check relies on the fact that the QueueNullTracer must not have a tracing context and therefore always returns a current stack size of -1 (??, think again)
                System.err.printf("WARNING: Offer failed. Possible queue corruption.%n");
            }
        }
        return traceMethod;
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     *
     * @return an {@link IndentablePrintStream}
     */
    @Override
    public IndentablePrintStream out() {
        return this.tracer.out();
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     *
     * @param level the level of the to be printed data
     * @return an {@link IndentablePrintStream}
     */
    @Override
    protected IndentablePrintStream out(int level) {
        return this.tracer.out(level);
    }

    /**
     * Delegates to the corresponding method of the wrapped tracer.
     */
    @Override
    public void clearCurrentTracingContext() {
        this.tracer.clearCurrentTracingContext();
    }
}
