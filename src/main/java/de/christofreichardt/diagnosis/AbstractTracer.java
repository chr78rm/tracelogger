/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.io.*;
import java.io.BufferedOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <div style="text-align: justify;">
 * <p>
 * Defines the basic behaviour of tracers. A tracer comes with a map which maps threads on so called
 * {@link TracingContext}s. Each of these {@link TracingContext}s manage a stack of {@link TraceMethod}s.
 * To be observed methods can be pushed on the method stack assigned to the current thread. When a method is
 * pushed on the stack a notification will be written to an output stream. When the method is popped from the
 * stack again a notification will be written together with the elapsed time too.
 * </p>
 * <p>
 * A {@link TracePrintStream} can be used to print additional output. Dependent on the size of the stack
 * the output can be intended thus providing a clearly arranged format. If the size of the stack exceeds a
 * configured limit the tracing output will be discarded. If the stack size decreases below this limit the output
 * will be printed again.
 * </p>
 * <p>
 * It's possible for a tracer to manage a stack for more than one thread. Then it's the responsibility of the
 * client to synchronize access to the output stream. However it is recommended
 * to use another tracer for each thread.
 * </p>
 * <p>
 * A distinction is made between tracing and logging. Log messages can be redirected to conventional logging systems
 * such as the logging system of the Java platform or log4j, see for an example {@link JDKLoggingRouter}.
 * </p>
 * <p>
 * Before a tracer generates output it must be opened and the tracing context of the current thread must
 * be initialised, e.g.:
 * </p>
 * <pre style="font-size: 12px">
 * final AbstractTracer tracer = new FileTracer("Example");
 * tracer.open();
 * try {
 *   class Foo {
 *     void bar() {
 *       tracer.entry("void", this, "bar()");
 *       try {
 *         tracer.out().printfIndentln("This is an example.");
 *       }
 *       finally {
 *         tracer.wayout();
 *       }
 *     }
 *   }
 *   Foo foo = new Foo();
 *   foo.bar(); // nothing will be printed because no tracing context has been provided
 *   tracer.initCurrentTracingContext(2, true);
 *   foo.bar(); // this will generate output
 * }
 * finally {
 *   tracer.close();
 * }</pre>
 * <p>
 * The generated output can be found at ./log/Example.log - whereas the directory ./log must exist - and looks like:
 * </p>
 * <pre style="font-size: 12px">
 * --&gt; TraceLog opened!
 *     Time     : Mi Apr 02 23:14:41 MESZ 2014
 *     Bufsize  : 512
 *     Autoflush: true
 *
 * ENTRY--void Foo[12275192].bar()--main[1]
 *   This is an example.
 * RETURN-void Foo[12275192].bar()--(+0ms)--(+0ms)--main[1]
 *
 * --&gt; TraceLog closing!
 *     Time     : Mi Apr 02 23:14:41 MESZ 2014
 * </pre>
 * </div>
 *
 * @author Christof Reichardt
 */
abstract public class AbstractTracer {

    final static public String VERSION = "1.10.0";

    /**
     * Indicates exceptional states within the AbstractTracer context.
     */
    public static class Exception extends java.lang.Exception {

        /**
         * Creates an Exception instance.
         *
         * @param msg the message
         */
        public Exception(String msg) {
            super(msg);
        }

        /**
         * Creates an Exception instance.
         *
         * @param cause the cause of the trouble
         */
        public Exception(Throwable cause) {
            super(cause);
        }
    }

    /** should be used as an (preferably) unique identifier amongst all tracer instances */
    final private String name;
    /** indicates if the output streams have been opened yet */
    private boolean opened = false;
    /** indicates whether the output stream will be flushed when leaving a method by {@link #wayout()} */
    private boolean autoflush = true; // TODO: think about volatile
    /** the buffer size */
    private int bufferSize = 512;

    /** the undesired output will be bypassed into this PrintStream */
    private final NullPrintStream nullPrintStream;
    /** used for buffering of the trace output */
    private BufferedOutputStream bufferedOutputStream = null;
    /** a specialised PrintStream suitable for indented output */
    private TracePrintStream tracePrintStream;

    /** provides access to the tracing contexts indexed by Threads */
    private final AbstractThreadMap threadMap = new ThreadLocalMap();
    /** provides access to configured tracing context information */
    final private Map<String, DebugConfig> debugConfigMap = new ConcurrentHashMap<>();

    /**
     * Constructor expects a name for the tracer, preferably unique.
     *
     * @param name the name of the tracer.
     */
    public AbstractTracer(String name) {
        if (name == null) {
            throw new NullPointerException("Need a name for the tracer.");
        }
        this.name = name;
        this.nullPrintStream = new NullPrintStream();
        this.tracePrintStream = new TracePrintStream(this.threadMap);
    }

    /**
     * The name of the tracer.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Indicates whether the actual {@link TracePrintStream} is opened.
     *
     * @return the opened
     */
    public boolean isOpened() {
        return opened;
    }

    /**
     * Derived classes may use this method to inform the base class that the actual {@link TracePrintStream}
     * is opened.
     *
     * @param opened the opened to set
     */
    protected void setOpened(boolean opened) {
        this.opened = opened;
    }

    /**
     * Indicates whether the output stream will be flushed when leaving a
     * method by {@link #wayout()}.
     *
     * @return the autoflush
     */
    public boolean isAutoflush() {
        return autoflush;
    }

    /**
     * Used during the configuration. Indicates whether the output stream will be flushed when leaving a
     * method by {@link #wayout()}.
     *
     * @param autoflush the autoflush to set
     */
    protected void setAutoflush(boolean autoflush) {
        this.autoflush = autoflush;
    }

    /**
     * Gives the buffer size of the actual {@link TracePrintStream}.
     *
     * @return the bufferSize
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Configures the buffer size of the actual {@link TracePrintStream}.
     *
     * @param bufferSize the bufferSize to set
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isOnline(String threadName) {
        DebugConfig debugConfig = this.debugConfigMap.get(threadName);
        if (Objects.nonNull(debugConfig)) {
            return debugConfig.isOnline();
        } else {
            throw new IllegalArgumentException(String.format("No such Thread[name=%s] configured.", threadName));
        }
    }

    public int getLevel(String threadName) {
        DebugConfig debugConfig = this.debugConfigMap.get(threadName);
        if (Objects.nonNull(debugConfig)) {
            return debugConfig.getLevel();
        } else {
            throw new IllegalArgumentException(String.format("No such Thread[name=%s] configured.", threadName));
        }
    }

    /**
     * A replacement for /dev/null.
     *
     * @return the nullPrintStream
     */
    protected NullPrintStream getNullPrintStream() {
        return nullPrintStream;
    }

    /**
     * Derived classes may use this getter to retrieve the buffer of the actual {@link TracePrintStream}.
     *
     * @return the bufferedOutputStream
     * @see #open()
     * @see #close()
     */
    protected BufferedOutputStream getBufferedOutputStream() {
        return bufferedOutputStream;
    }

    /**
     * Derived classes may use this setter to inform the base class about the buffer of the
     * actual {@link TracePrintStream}.
     *
     * @param bufferedOutputStream the bufferedOutputStream to set
     * @see #open()
     * @see #close()
     */
    protected void setBufferedOutputStream(BufferedOutputStream bufferedOutputStream) {
        this.bufferedOutputStream = bufferedOutputStream;
    }

    /**
     * Derived classes may use this getter to retrieve the actual {@link TracePrintStream}.
     *
     * @return the tracePrintStream
     * @see #open()
     * @see #close()
     */
    protected TracePrintStream getTracePrintStream() {
        return tracePrintStream;
    }

    /**
     * Derived classes may use this setter to inform the base class about the actual {@link TracePrintStream}.
     *
     * @param tracePrintStream the tracePrintStream to set
     * @see #open()
     * @see #close()
     */
    protected void setTracePrintStream(TracePrintStream tracePrintStream) {
        this.tracePrintStream = tracePrintStream;
    }

    /**
     * Provides access to the tracing contexts indexed by Threads.
     *
     * @return the threadMap
     */
    protected AbstractThreadMap getThreadMap() {
        return threadMap;
    }

    /**
     * Reads the configuration for this particular tracer instance by evaluating the given node with the given xpath engine.
     *
     * @param xpath the xpath engine
     * @param node  the config node
     * @throws javax.xml.xpath.XPathExpressionException                indicates xpath problems
     * @throws de.christofreichardt.diagnosis.AbstractTracer.Exception indicates problems when configuring certain tracer instances
     */
    protected void readConfiguration(XPath xpath, Node node) throws XPathExpressionException, AbstractTracer.Exception {
        this.autoflush = "true".equals(xpath.evaluate("./dns:AutoFlush/text()", node, XPathConstants.STRING));
        this.bufferSize = Integer.parseInt((String) xpath.evaluate("./dns:BufSize/text()", node, XPathConstants.STRING));

        System.out.println("this.autoflush = " + this.autoflush);
        System.out.println("this.bufferSize = " + this.bufferSize);

        NodeList threadNodes = (NodeList) xpath.evaluate("./dns:Context/dns:Thread", node, XPathConstants.NODESET);
        for (int i = 0; i < threadNodes.getLength(); i++) {
            String threadName = threadNodes.item(i).getAttributes().getNamedItem("name").getNodeValue();

            boolean online = "true".equals(xpath.evaluate("./dns:Online/text()", threadNodes.item(i), XPathConstants.STRING));
            int debugLevel = Integer.parseInt((String) xpath.evaluate("./dns:DebugLevel/text()", threadNodes.item(i), XPathConstants.STRING));

            System.out.println("(*-*)");
            System.out.println("threadName = " + threadName);
            System.out.println("online = " + online);
            System.out.println("debugLevel = " + debugLevel);

            this.debugConfigMap.put(threadName, new DebugConfig(online, debugLevel));
        }
    }

    /**
     * Derived classes should provide code that opens the respective output streams.
     */
    public abstract void open();

    /**
     * Derived classes should provide code that closes the output streams.
     */
    public abstract void close();

    /**
     * Returns some kind of {@link IndentablePrintStream} based upon the current managed stack size, the configured debug
     * level and the online state of the current tracing context. This is the {@link NullPrintStream} if the stack size is greater
     * than the debug level or if the thread is offline. Otherwise, it's the {@link TracePrintStream}.
     *
     * @return an {@link IndentablePrintStream}
     */
    public IndentablePrintStream out() {
        return out(this.threadMap.getCurrentStackSize());
    }

    /**
     * Returns some kind of {@link IndentablePrintStream} based upon the given level, the configured debug level and the
     * online state of the current tracing context. It will be the {@link NullPrintStream} if the given level is greater
     * than the debug level or if the thread is offline. Otherwise, it's the {@link TracePrintStream}.
     *
     * @param level the level of the to be printed data
     * @return an {@link IndentablePrintStream}
     */
    protected IndentablePrintStream out(int level) {
        IndentablePrintStream printStream;

        if (level >= 0) {
            TracingContext tracingContext = this.threadMap.getCurrentTracingContext();
            if (tracingContext != null && tracingContext.isOnline() && tracingContext.getDebugLevel() >= level) {
                printStream = this.tracePrintStream;
            } else {
                printStream = this.nullPrintStream;
            }
        } else {
            printStream = this.nullPrintStream;
        }

        return printStream;
    }

    /**
     * Indicates an entering of a method.
     *
     * @param methodSignature the signature of the method as string representation
     * @return the TraceMethod which has been put onto the stack - a mere data object for internal use primarily. May be null.
     * @deprecated use {@link #entry(String returnType, Class clazz, String methodSignature)} or
     * {@link #entry(String returnType, Object object, String methodSignature)}
     */
    @Deprecated
    public TraceMethod entry(String methodSignature) {
        out().runWithLock(() -> out().printfIndentln("ENTRY--" + methodSignature + "--" + Thread.currentThread().getName() + "[" + Thread.currentThread().getId() + "]"));

        TraceMethod traceMethod = null;
        try {
            traceMethod = new TraceMethod(methodSignature);
            if (!this.threadMap.push(traceMethod)) {
                traceMethod = null;
            }
        } catch (AbstractThreadMap.RuntimeException ex) {
            logMessage(LogLevel.SEVERE, "Stacksize is exceeded. Tracing is off.", this.getClass(), "entry()");
        }

        return traceMethod;
    }

    /**
     * Prints the method signature on the {@link de.christofreichardt.diagnosis.io.IndentablePrintStream}.
     *
     * @param methodSignature the method signature to be printed
     */
    private void printMethodEntry(String methodSignature) {
        out().runWithLock(() -> out().printfIndentln("ENTRY--" + methodSignature + "--" + Thread.currentThread().getName() + "[" + Thread.currentThread().getId() + "]"));
    }

    /**
     * Indicates an entering of a method which belongs to an object. If a {@link TracingContext} exists for the current thread a {@link TraceMethod} object
     * will be created and thereupon pushed onto the stack of a {@link ThreadMap}.
     *
     * @param returnType      the return type of the method as string representation
     * @param object          the object that owns the method
     * @param methodSignature the remaining method signature (without return type) inclusive parameter as string representation
     * @return the TraceMethod which has been put onto the stack - a mere data object for internal use primarily. May be null.
     */
    public TraceMethod entry(String returnType, Object object, String methodSignature) {
        TraceMethod traceMethod;

        if (object != null) {
            traceMethod = new TraceMethod(returnType, object, methodSignature);
            methodSignature = traceMethod.getSignature();
        } else {
            methodSignature = returnType + " " + methodSignature;
            traceMethod = new TraceMethod(methodSignature);
        }

        printMethodEntry(methodSignature);

        try {
            if (!this.threadMap.push(traceMethod)) {
                traceMethod = null;
            }
        } catch (AbstractThreadMap.RuntimeException ex) {
            logMessage(LogLevel.SEVERE, "Stacksize is exceeded. Tracing is off.", this.getClass(), "entry()");
        }

        return traceMethod;
    }

    /**
     * Indicates an entering of a method which belongs to a class. If a {@link TracingContext} exists for the current thread a {@link TraceMethod} object
     * will be created and thereupon pushed onto the stack of a {@link ThreadMap}.
     *
     * @param returnType      the return type of the method as string representation
     * @param clazz           the class to which that method belong
     * @param methodSignature the remaining method signature (without return type) inclusive parameter as string representation
     * @return the TraceMethod which has been put onto the stack - a mere data object for internal use primarily. May be null.
     */
    public TraceMethod entry(String returnType, Class<?> clazz, String methodSignature) {
        TraceMethod traceMethod;

        if (clazz != null) {
            traceMethod = new TraceMethod(returnType, clazz, methodSignature);
            methodSignature = traceMethod.getSignature();
        } else {
            methodSignature = returnType + " " + methodSignature;
            traceMethod = new TraceMethod(methodSignature);
        }

        printMethodEntry(methodSignature);

        try {
            if (!this.threadMap.push(traceMethod)) {
                traceMethod = null;
            }
        } catch (AbstractThreadMap.RuntimeException ex) {
            logMessage(LogLevel.SEVERE, "Stacksize is exceeded. Tracing is off.", this.getClass(), "entry()");
        }

        return traceMethod;
    }

    /**
     * Indicates the exiting of a method.
     *
     * @return the TraceMethod which has been popped from the stack - a mere data object for internal use primarily. May be null.
     */
    public TraceMethod wayout() {
        TraceMethod traceMethod = null;

        try {
            traceMethod = this.threadMap.pop();
            if (traceMethod != null) {
                out().lock();
                try {
                    out().printIndentln("RETURN-" + traceMethod.getSignature() + "--(+" + traceMethod.getElapsedTime() + "ms)--" + "(+" + traceMethod.getElapsedCpuTime() + "ms)--" + Thread.currentThread().getName() + "[" + Thread.currentThread().getId() + "]");
                    if (this.autoflush) {
                        out().flush();
                    }
                } finally {
                    out().unlock();
                }
            }
        } catch (AbstractThreadMap.RuntimeException ex) {
            logMessage(LogLevel.SEVERE, "Stack is corrupted. Tracing is off.", this.getClass(), "wayout()");
        }

        return traceMethod;
    }

    /**
     * Logs a message with the given logLevel and the originating class.
     *
     * @param logLevel   one of the predefined levels INFO, WARNING, ERROR, FATAL and SEVERE
     * @param message    the to be logged message
     * @param clazz      the originating class
     * @param methodName the originating method
     */
    public void logMessage(LogLevel logLevel, String message, Class<?> clazz, String methodName) {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        char[] border = new char[logLevel.toString().length() + 4];
        Arrays.fill(border, '-');
        border[0] = '+';
        border[border.length - 1] = '+';

        this.tracePrintStream.lock();
        try {
            this.tracePrintStream.println(border);
            this.tracePrintStream.printf("| %s |  [%s] [%d,%s] [%s] [%s] \"%s\"%n", logLevel, timeStamp, Thread.currentThread().getId(),
                    Thread.currentThread().getName(), clazz.getName(), methodName, message);
            this.tracePrintStream.println(border);
        } finally {
            this.tracePrintStream.unlock();
        }
    }

    /**
     * Logs an exception with the given logLevel and the originating class.
     *
     * @param logLevel   one of the predefined levels INFO, WARNING, ERROR, FATAL and SEVERE
     * @param throwable  the to be logged throwable
     * @param clazz      the originating class
     * @param methodName the name of the relevant method
     */
    public void logException(LogLevel logLevel, Throwable throwable, Class<?> clazz, String methodName) {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        char[] border = new char[logLevel.toString().length() + 4];
        Arrays.fill(border, '-');
        border[0] = '+';
        border[border.length - 1] = '+';

        String message;
        if (throwable.getMessage() != null) {
            message = throwable.getMessage().trim();
            message = message.replace(System.getProperty("line.separator"), " => ");
        } else {
            message = "No message.";
        }

        this.tracePrintStream.lock();
        try {
            this.tracePrintStream.println(border);
            this.tracePrintStream.printf("| %s |  [%s] [%d,%s] [%s] [%s] \"%s\"%n", logLevel, timeStamp, Thread.currentThread().getId(),
                    Thread.currentThread().getName(), clazz.getName(), methodName, message);
            this.tracePrintStream.println(border);
            throwable.printStackTrace(this.tracePrintStream);
        } finally {
            this.tracePrintStream.unlock();
        }
    }

    /**
     * Initialises the current tracing context with the given debugLevel and online state.
     *
     * @param debugLevel controls the extent of the output
     * @param online     a value of false delivers no output of the current thread at all whereas a value of true delivers output controlled by debugLevel
     */
    public void initCurrentTracingContext(int debugLevel, boolean online) {

        // todo: prevent manually creation of tracing contexts by configuration?!

        TracingContext tracingContext = this.threadMap.getCurrentTracingContext();
        if (tracingContext == null) {
            System.out.println(formatContextInfo(debugLevel, online));
            tracingContext = new TracingContext(debugLevel, online);
            this.threadMap.setCurrentTracingContext(tracingContext);
        } else {
            tracingContext.setDebugLevel(debugLevel);
            tracingContext.setOnline(online);
        }
    }

    /**
     * Initialises the current tracing context by taking the values for debugLevel and online from the configured
     * debug map.
     */
    public void initCurrentTracingContext() {
        TracingContext tracingContext = this.threadMap.getCurrentTracingContext();
        if (tracingContext == null) {
            if (this.debugConfigMap.containsKey(Thread.currentThread().getName())) {
                DebugConfig debugConfig = this.debugConfigMap.get(Thread.currentThread().getName());
                System.out.println(formatContextInfo(debugConfig.getLevel(), debugConfig.isOnline()));
                tracingContext = new TracingContext(debugConfig);
                this.threadMap.setCurrentTracingContext(tracingContext);
            }
        }
    }

    /**
     * Removes the current tracing context, that is - for example - subsequent calls to {@link #out()} from the current thread will
     * return the NullPrintStream.
     */
    public void clearCurrentTracingContext() {
        this.threadMap.removeCurrentTracingContext();
    }

    private String formatContextInfo(int debugLevel, boolean online) {
        Formatter formatter = new Formatter();
        formatter.format("TraceLogger[%s]: Initialising tracing context for Thread[id=%d,name=%s] with debugLevel=%d and online=%b ... ",
                this.name, Thread.currentThread().getId(), Thread.currentThread().getName(), debugLevel, online);

        return formatter.toString();
    }

    /**
     * Gives a string representation about the error state of the {@link IndentablePrintStream} of this Tracer instance.
     *
     * @return a formatted status line
     */
    protected String formatStreamErrorState() {
        Formatter formatter = new Formatter();
        formatter.format("TraceLogger[%s]: Stream error state = %s.", this.name, this.tracePrintStream.checkError() ? "bad" : "ok");

        return formatter.toString();
    }

    /**
     * Gives a string representation about the version of this library.
     *
     * @return a formatted status line
     */
    protected String formatVersionInfo() {
        Formatter formatter = new Formatter();
        formatter.format("TraceLogger[%s]: Version = %s.", this.name, VERSION);

        return formatter.toString();
    }
}
