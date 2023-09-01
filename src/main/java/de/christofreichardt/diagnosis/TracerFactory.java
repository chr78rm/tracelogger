/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.file.FileTracer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <div style="text-align: justify">
 * <p>
 * A factory and holder of tracers. Tracers will be created according to a given (XML-)configuration. So long as no configuration has been read
 * some methods provide a default tracer. This default tracer traces nothing and routes log messages to the core logging facilities of the Java platform,
 * see <a href="http://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html">java.util.logging</a> and {@link JDKLoggingRouter}.
 * </p>
 * <p>
 * The configuration file consists of four main sections. You can put tracer into a pool and access them by name. Second you may redefine the default tracer.
 * Third you can map threads on tracer and subsequently access the primary tracer for a given thread. Or you may configure a blocking queue of tracer for
 * multi-threading environments for which you cannot control thread creation. Consider for example the following definitions:
 * </p>
 * <pre style="font-size: 12px">
 * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
 * &lt;TraceConfig xmlns="http://www.christofreichardt.de/java/tracer"&gt;
 *   &lt;Pool&gt;
 *     &lt;TraceLogger name="ExampleTracer" class="de.christofreichardt.diagnosis.file.FileTracer"&gt;
 *       &lt;LogDir&gt;./log/&lt;/LogDir&gt;
 *       &lt;AutoFlush&gt;true&lt;/AutoFlush&gt;
 *       &lt;BufSize&gt;1024&lt;/BufSize&gt;
 *       &lt;Limit&gt;1048576&lt;/Limit&gt;
 *       &lt;Context&gt;
 *         &lt;Thread name="main"&gt;
 *           &lt;Online&gt;true&lt;/Online&gt;
 *           &lt;DebugLevel&gt;5&lt;/DebugLevel&gt;
 *         &lt;/Thread&gt;
 *       &lt;/Context&gt;
 *     &lt;/TraceLogger&gt;
 *   &lt;/Pool&gt;
 *   &lt;Map&gt;
 *     &lt;Threads&gt;
 *       &lt;Thread name="main"&gt;
 *         &lt;TraceLogger ref="ExampleTracer" /&gt;
 *       &lt;/Thread&gt;
 *     &lt;/Threads&gt;
 *   &lt;/Map&gt;
 * &lt;/TraceConfig&gt;</pre>
 * <p>
 * The definitions above make use of the first and the third section. A {@link FileTracer} has been configured. Its outputfile is located at
 * ./log/ExampleTracer.log. The tracer is in autoflush mode, that is every time an observed method is popped from the stack the output stream will be flushed.
 * The tracer will back up its file when it reaches the size of one MebiByte (1024*1024 Byte). The 'ExampleTracer' is interested in output from the
 * main-Thread up to a stack size of five. Note that this is not the call stack of the Java Virtual Machine. You may put only methods you are interested in on
 * a separate stack managed by a {@link TracingContext}. The main-Thread has been mapped on the 'ExampleTracer'. Therefore you may invoke a convenience
 * method to retrieve the tracer for this thread. Assuming you put the configuration file into ./config/ExampleConfig.xml, the TracerFactory can be
 * configured and used like this from the main-Thread:
 * </p>
 * <pre style="font-size: 12px">
 * File configFile = new File("." + File.separator + "config" + File.separator + "ExampleConfig.xml");
 * TracerFactory.getInstance().readConfiguration(configFile);
 * final AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
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
 *   tracer.initCurrentTracingContext(); // the configured tracing context will be used
 *   foo.bar(); // this will generate output
 * }
 * finally {
 *   tracer.close();
 * }</pre>
 * <p>
 * The generated output can be found at ./log/ExampleTracer.log - whereas the directory ./log must exist - and looks like:
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
 *     Time     : Mi Apr 02 23:14:41 MESZ 2014</pre>
 * <p>
 * This approach makes sense if you control the creation of threads. Keep in mind that the Java Virtual Machine doesn't guarantee the uniqueness of thread names.
 * That is when you invoke <code style="">TracerFactory.getInstance().getCurrentPoolTracer()</code> from another thread called 'main' later on,
 * you will get the default tracer which routes log messages (but not the tracing output) to the JDK logger.
 * </p>
 * </div>
 *
 * @author Christof Reichardt
 */
public class TracerFactory {

    private static final class InstanceHolder {
        static final TracerFactory INSTANCE = new TracerFactory();
    }

    /**
     * Retrieves the single TracerFactory.
     *
     * @return the single TracerFactory
     */
    public static TracerFactory getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static final class ErrorHandler implements org.xml.sax.ErrorHandler {
        @Override
        public void warning(SAXParseException ex) {
            System.err.println(ex.getMessage());
        }

        @Override
        public void error(SAXParseException ex) throws SAXException {
            throw new SAXException(ex);
        }

        @Override
        public void fatalError(SAXParseException ex) throws SAXException {
            throw new SAXException(ex);
        }
    }

    /**
     * Base exception class for all exceptional situations within the context of the TracerFactory
     */
    public static class Exception extends java.lang.Exception {

        /**
         * Constructor expects error message.
         *
         * @param msg the error message
         */
        public Exception(String msg) {
            super(msg);
        }

        /**
         * Constructor expects a throwable which is the cause of the trouble.
         *
         * @param cause the originator of the trouble
         */
        public Exception(Throwable cause) {
            super(cause);
        }
    }

    /**
     * <div style="text-align: justify">
     * If appropriately configured it enables access to a blocking deque of tracers. The usage of the queue methods ({@link TracerFactory#takeTracer()},
     * {@link TracerFactory#offerTracer(de.christofreichardt.diagnosis.QueueTracer)} is one option for multi-threaded environments. By default a QueueNullTracer will
     * be returned which routes to the core logging facilities of the Java platform.
     * </div>
     */
    protected class Queue {
        private final boolean enabled;
        private final int size;
        private final String className;
        private final BlockingDeque<QueueTracer<? extends AbstractTracer>> blockingTracerDeque;
        private final QueueNullTracer queueNullTracer = new QueueNullTracer(TracerFactory.this.defaultTracer);
        private final ThreadLocal<QueueTracer<? extends AbstractTracer>> currentTracer;

        Queue() {
            this.enabled = false;
            this.size = 0;
            this.blockingTracerDeque = null;
            this.className = null;
            this.currentTracer = null;
        }

        Queue(Node node) throws XPathExpressionException, TracerFactory.Exception, AbstractTracer.Exception {
            this.enabled = TracerFactory.this.xpath.evaluate("./dns:Enabled", node, XPathConstants.NODE) != null;
            if (this.enabled) {
                this.size = Integer.parseInt((String) TracerFactory.this.xpath.evaluate("./dns:Size", node, XPathConstants.STRING));
                this.className = (String) TracerFactory.this.xpath.evaluate("./dns:TraceLogger/@class", node, XPathConstants.STRING);
                this.blockingTracerDeque = new LinkedBlockingDeque<>(this.size);
                this.currentTracer = new ThreadLocal<>();
                init(node);
            } else {
                this.size = 0;
                this.className = null;
                this.blockingTracerDeque = null;
                this.currentTracer = null;
            }
        }

        private void init(Node node) throws TracerFactory.Exception, XPathExpressionException, AbstractTracer.Exception {
            try {
                for (int i = 0; i < this.size; i++) {
                    Class<?> clazz = Class.forName(this.className);
                    if (!QueueTracer.class.isAssignableFrom(clazz))
                        throw new TracerFactory.Exception("Need a QueueTracer class but found '" + clazz.getName() + "'.");
                    @SuppressWarnings("unchecked")
                    Class<QueueTracer<? extends AbstractTracer>> tracerClass = (Class<QueueTracer<? extends AbstractTracer>>) clazz;
                    if (QueueNullTracer.class.isAssignableFrom(tracerClass))
                        throw new TracerFactory.Exception("No QueueNullTracer allowed here.");
                    String tracerName = (String) TracerFactory.this.xpath.evaluate("./dns:TraceLogger/@name", node, XPathConstants.STRING);
                    Constructor<QueueTracer<? extends AbstractTracer>> constructor = tracerClass.getConstructor(String.class);
                    QueueTracer<? extends AbstractTracer> queueTracer = constructor.newInstance(tracerName + i);
                    queueTracer.readConfiguration(TracerFactory.this.xpath, node);
                    this.blockingTracerDeque.offerLast(queueTracer);
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException ex) {
                throw new TracerFactory.Exception(ex);
            }
        }
    }

    static final NullTracer NULLTRACER = new JDKLoggingRouter();

    final private Schema traceConfigSchema;
    final private Map<String, Element> threadName2Element = new HashMap<>();
    final private Map<String, AbstractTracer> tracerPool = new HashMap<>();
    final private Map<Long, AbstractTracer> tracerMap = new ConcurrentHashMap<>();
    final private Set<String> threadNames = Collections.newSetFromMap(new ConcurrentHashMap<>());
    final private XPath xpath = XPathFactory.newInstance().newXPath();
    private NullTracer defaultTracer = TracerFactory.NULLTRACER;

    final private ReentrantReadWriteLock poolLock = new ReentrantReadWriteLock();
    final private Lock poolReadLock = this.poolLock.readLock();
    final private Lock poolWriteLock = this.poolLock.writeLock();

    final private ReentrantReadWriteLock queueLock = new ReentrantReadWriteLock();
    final private Lock queueReadLock = this.queueLock.readLock();
    final private Lock queueWriteLock = this.queueLock.writeLock();
    private Queue queueConfig = new Queue();

    private TracerFactory() {
        this.traceConfigSchema = loadTraceConfigSchema();
        this.xpath.setNamespaceContext(new TracerConfigNamespaceContextImpl());
    }

    /**
     * @return the defaultTracer
     */
    public NullTracer getDefaultTracer() {
        return defaultTracer;
    }

    /**
     * @return the size of the tracer queue
     */
    public int getQueueSize() {
        this.queueReadLock.lock();
        try {
            return this.queueConfig.size;
        } finally {
            this.queueReadLock.unlock();
        }
    }

    /**
     * @return indicates if the tracer queue is enabled
     */
    public boolean isQueueEnabled() {
        this.queueReadLock.lock();
        try {
            return this.queueConfig.enabled;
        } finally {
            this.queueReadLock.unlock();
        }
    }

    /**
     * @return the classname of the employed QueueTracer
     */
    public String getQueueTracerClassname() {
        this.queueReadLock.lock();
        try {
            return this.queueConfig.className;
        } finally {
            this.queueReadLock.unlock();
        }
    }

    private Schema loadTraceConfigSchema() {
        InputStream inputStream = TracerFactory.class.getClassLoader().getResourceAsStream("de/christofreichardt/diagnosis/TraceConfigSchema.xsd");
        StreamSource streamSource = new StreamSource(inputStream);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema = schemaFactory.newSchema(streamSource);
        } catch (SAXException ex) {
            ex.printStackTrace(System.err);
        }

        return schema;
    }

    /**
     * Reads the given configuration file, validates it against an XML-Schema and creates the tracer pool, its mappings and the queue accordingly.
     * This method should normally be invoked once at program start. Multiple calls with the same configuration file leads to instantiations of new tracer objects
     * and mappings which will replace the old tracers and their mappings.
     *
     * @param configFile the configuration file
     * @throws TracerFactory.Exception indicates a configuration problem
     * @throws IOException             indicates an I/O problem, e.g. a missing configuration file
     */
    public void readConfiguration(File configFile) throws TracerFactory.Exception, IOException {
        if (!configFile.exists())
            throw new FileNotFoundException(configFile + "doesn't exist.");
        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            readConfiguration(fileInputStream);
        }
    }

    /**
     * Reads the configuration from the given InputStream.
     *
     * @param inputStream the input stream providing the configuration.
     * @throws TracerFactory.Exception indicates a configuration problem
     * @see TracerFactory#readConfiguration(java.io.File)
     */
    public void readConfiguration(InputStream inputStream) throws TracerFactory.Exception {
        if (this.traceConfigSchema == null)
            System.err.println("CAUTION: Unable to validate the given configuration against a schema.");

        DocumentBuilderFactory builderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setXIncludeAware(false);

        try {
            DocumentBuilder parser = builderFactory.newDocumentBuilder();
            Document tracerConfigDoc = parser.parse(inputStream);

            if (this.traceConfigSchema != null) {
                DOMSource domSource = new DOMSource(tracerConfigDoc);
                Validator traceConfigValidator = this.traceConfigSchema.newValidator();
                TracerFactory.ErrorHandler errorHandler = new TracerFactory.ErrorHandler();
                traceConfigValidator.setErrorHandler(errorHandler);
                traceConfigValidator.validate(domSource);
            }

            this.poolWriteLock.lock();
            try {
                NodeList tracerNodes = (NodeList) this.xpath.evaluate("/dns:TraceConfig/dns:Pool/dns:TraceLogger", tracerConfigDoc.getDocumentElement(), XPathConstants.NODESET);
                System.out.println();
                System.out.println("Configured Pool Tracers = " + tracerNodes.getLength());
                for (int i = 0; i < tracerNodes.getLength(); i++) {
                    System.out.println();
                    System.out.println("(+) " + (i + 1) + ". TraceLogger");
                    Element tracerElement = (Element) tracerNodes.item(i);
                    if (!tracerElement.hasAttribute("name"))
                        throw new TracerFactory.Exception("Missing 'name' attribute.");
                    String name = tracerElement.getAttribute("name");
                    String className = tracerElement.getAttribute("class");
                    System.out.println("name = " + name);
                    System.out.println("className = " + className);
                    Class<?> tracerClass = Class.forName(className);
                    if (!AbstractTracer.class.isAssignableFrom(tracerClass))
                        throw new TracerFactory.Exception(String.format("Illegal tracer class: '%s'", className));
                    @SuppressWarnings("unchecked")
                    AbstractTracer tracer = createTracer((Class<? extends AbstractTracer>) tracerClass, name);
                    tracer.readConfiguration(this.xpath, tracerElement);
                    this.tracerPool.put(name, tracer);
                }

                NodeList threadNodes = (NodeList) this.xpath.evaluate("/dns:TraceConfig/dns:Map/dns:Threads/dns:Thread", tracerConfigDoc.getDocumentElement(), XPathConstants.NODESET);
                System.out.println();
                System.out.println("Configured Tracermappings = " + threadNodes.getLength());
                for (int i = 0; i < threadNodes.getLength(); i++) {
                    System.out.println();
                    System.out.println("(+) " + (i + 1) + ". Mapping");
                    Element threadElement = (Element) threadNodes.item(i);
                    String threadName = threadElement.getAttribute("name");
                    String referencedTracerName = (String) this.xpath.evaluate("./dns:TraceLogger/@ref", threadElement, XPathConstants.STRING);
                    System.out.println(threadName + " => " + referencedTracerName);
                    this.threadName2Element.put(threadName, threadElement);
                }

                Node defaultTracerNode = (Node) this.xpath.evaluate("/dns:TraceConfig/dns:DefaultTracer", tracerConfigDoc.getDocumentElement(), XPathConstants.NODE);
                if (defaultTracerNode != null) {
                    String className = ((Element) defaultTracerNode).getAttribute("class");
                    Class<?> tracerClass = Class.forName(className);
                    if (!NullTracer.class.isAssignableFrom(tracerClass))
                        throw new TracerFactory.Exception("Requiring a NullTracer as default tracer!");
                    @SuppressWarnings("unchecked")
                    NullTracer nullTracer = createTracer((Class<? extends NullTracer>) tracerClass);
                    this.defaultTracer = nullTracer;
                } else {
                    this.defaultTracer = TracerFactory.NULLTRACER;
                }
            } finally {
                this.poolWriteLock.unlock();
            }

            this.queueWriteLock.lock();
            try {
                Node queueNode = (Node) this.xpath.evaluate("/dns:TraceConfig/dns:Queue", tracerConfigDoc.getDocumentElement(), XPathConstants.NODE);
                if (queueNode != null) {
                    this.queueConfig = new Queue(queueNode);
                } else {
                    this.queueConfig = new Queue();
                }
            } finally {
                this.queueWriteLock.unlock();
            }
        } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException |
                 ClassNotFoundException | AbstractTracer.Exception ex) {
            throw new TracerFactory.Exception(ex);
        }
    }

    private NullTracer createTracer(Class<? extends NullTracer> tracerClass) throws TracerFactory.Exception {
        try {
            Constructor<? extends NullTracer> constructor = tracerClass.getConstructor();
            return constructor.newInstance();
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException ex) {
            throw new TracerFactory.Exception(ex);
        }
    }

    private AbstractTracer createTracer(Class<? extends AbstractTracer> tracerClass, String name) throws TracerFactory.Exception {
        try {
            Constructor<? extends AbstractTracer> constructor = tracerClass.getConstructor(String.class);
            return constructor.newInstance(name);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException ex) {
            throw new TracerFactory.Exception(ex);
        }
    }

    private AbstractTracer getTracerByName(String name) throws TracerFactory.Exception {
        if (!this.tracerPool.containsKey(name)) {
            throw new TracerFactory.Exception("Unknown tracer: '" + name + "'");
        }

        return this.tracerPool.get(name);
    }

    /**
     * Returns the pooled tracer with the given name.
     *
     * @param name the name of the desired tracer
     * @return the pooled tracer
     * @throws TracerFactory.Exception if no tracer exists with the given name
     */
    public AbstractTracer getTracer(String name) throws TracerFactory.Exception {
        this.poolReadLock.lock();
        try {
            return getTracerByName(name);
        } finally {
            this.poolReadLock.unlock();
        }
    }

    /**
     * Returns the mapped tracer for the given thread. The given Thread object is used as key to a map. If no tracer can be found within the map
     * for the given Thread the configuration will be searched for the thread's name. That is initially the resolution is based on thread names (prior to program
     * execution no threads do exist but someone may know the to be used thread names). If there isn't an entry for a given Thread but the name of the
     * thread has been encountered before (thread names aren't unique) a NullTracer will be returned.
     *
     * @param thread the thread for which a tracer is searched
     * @return the mapped tracer for the given thread
     */
    public AbstractTracer getTracer(Thread thread) {
        this.poolReadLock.lock();
        try {
            AbstractTracer tracer;

            if (!this.tracerMap.containsKey(thread.getId())) {
                if (this.threadNames.contains(thread.getName())) { // non-unique thread name, first come, first served
                    System.err.printf("WARNING: Duplicate thread name \"%s\" encountered.%n", thread.getName());
                    tracer = this.defaultTracer;
                } else {
                    if (this.threadName2Element.containsKey(thread.getName())) {
                        try {
                            Element threadElement = this.threadName2Element.get(thread.getName());
                            String referencedTracerName = (String) this.xpath.evaluate("./dns:TraceLogger/@ref", threadElement, XPathConstants.STRING);
                            tracer = getTracerByName(referencedTracerName);
                            this.tracerMap.put(thread.getId(), tracer);
                            this.threadNames.add(thread.getName());
                        } catch (XPathExpressionException | TracerFactory.Exception ex) {
                            tracer = this.defaultTracer;
                        }
                    } else
                        tracer = this.defaultTracer;
                }
            } else
                tracer = this.tracerMap.get(thread.getId());

            return tracer;
        } finally {
            this.poolReadLock.unlock();
        }
    }

    /**
     * Returns the mapped tracer for the current thread.
     *
     * @return the mapped tracer for the current thread
     * @see #getTracer(java.lang.Thread)
     */
    public AbstractTracer getCurrentPoolTracer() {
        return getTracer(Thread.currentThread());
    }

    /**
     * Clears the pool, the mappings and the queue.
     */
    public void reset() { // TODO: Think about closing all present tracers prior to clearing the maps and pools
        this.poolWriteLock.lock();
        try {
            this.defaultTracer = TracerFactory.NULLTRACER;
            this.threadName2Element.clear();
            this.threadNames.clear();
            this.tracerMap.clear();
            this.tracerPool.clear();
        } finally {
            this.poolWriteLock.unlock();
        }

        this.queueWriteLock.lock();
        try {
            this.queueConfig = new Queue();
        } finally {
            this.queueWriteLock.unlock();
        }
    }

    /**
     * Opens all pooled tracers.
     */
    public void openPoolTracer() {
        this.poolWriteLock.lock();
        try {
            for (AbstractTracer tracer : this.tracerPool.values()) {
                tracer.open();
            }
        } finally {
            this.poolWriteLock.unlock();
        }
    }

    /**
     * Closes all pooled tracers.
     */
    public void closePoolTracer() {
        this.poolWriteLock.lock();
        try {
            for (AbstractTracer tracer : this.tracerPool.values()) {
                tracer.close();
            }
        } finally {
            this.poolWriteLock.unlock();
        }
    }

    /**
     * Takes the tracer from the head of the deque. If the deque is empty the methods blocks until a tracer will become available. By default, a
     * QueueTracer wrapping a NullTracer will be (non-blocking) delivered.
     *
     * @return the tracer from the head of the deque
     */
    public QueueTracer<? extends AbstractTracer> takeTracer() { // TODO: think about a (boolean) parameter which indicates whether the tracing context should be automatically created
        this.queueReadLock.lock();
        try {
            QueueTracer<? extends AbstractTracer> tracer;
            if (this.queueConfig.enabled) {
                try {
                    tracer = this.queueConfig.blockingTracerDeque.takeFirst();
//          this.queueConfig.tracerMap.put(Thread.currentThread(), tracer);
                    this.queueConfig.currentTracer.set(tracer);
                } catch (InterruptedException ex) {
                    System.err.printf("Interrupted when waiting for a QueueTracer... %n");
                    tracer = this.queueConfig.queueNullTracer;
                }
            } else {
                tracer = this.queueConfig.queueNullTracer;
            }

            return tracer;
        } finally {
            this.queueReadLock.unlock();
        }
    }

    /**
     * Used to enqueue a tracer which has been previously retrieved by a call to {@link #takeTracer()}.
     *
     * @param tracer the to be enqueued tracer
     * @return indicates if the tracer has been enqueued (true) or has been discarded (false)
     */
    protected boolean offerTracer(QueueTracer<? extends AbstractTracer> tracer) {
        boolean success = false;
        this.queueReadLock.lock();
        try {
            if (this.queueConfig.enabled  && !(tracer instanceof QueueNullTracer) ) {
                success = this.queueConfig.blockingTracerDeque.offerLast(tracer);
                if (success) {
                    this.queueConfig.currentTracer.remove();
                }
            }

            return success;
        } finally {
            this.queueReadLock.unlock();
        }
    }

    /**
     * Tries to open all enqueued QueueTracer.
     *
     * @return true if all configured tracers has been opened, false otherwise
     */
    public boolean openQueueTracer() {
        final int TRIALS = 5;
        int tracerCounter = 0, trialCounter = 0;
        boolean success = false;

        do {
            this.queueWriteLock.lock();
            try {
                if (this.queueConfig.enabled) {
                    for (QueueTracer<?> queueTracer : this.queueConfig.blockingTracerDeque) {
                        if (!queueTracer.isOpened()) {
                            queueTracer.open();
                            tracerCounter++;
                            if (tracerCounter == this.queueConfig.size)
                                success = true;
                        }
                    }
                }
            } finally {
                this.queueWriteLock.unlock();
            }
            trialCounter++;
        } while (tracerCounter < this.queueConfig.size && trialCounter < TRIALS);

        return success;
    }

    /**
     * Tries to close all enqueued QueueTracer.
     *
     * @return true if all configured tracers has been closed, false otherwise
     */
    public boolean closeQueueTracer() {
        final int TRIALS = 5;
        int tracerCounter = 0, trialCounter = 0;
        boolean success = false;

        do {
            this.queueWriteLock.lock();
            try {
                if (this.queueConfig.enabled) {
                    for (QueueTracer<?> queueTracer : this.queueConfig.blockingTracerDeque) {
                        if (queueTracer.isOpened()) {
                            queueTracer.close();
                            tracerCounter++;
                            if (tracerCounter == this.queueConfig.size)
                                success = true;
                        }
                    }
                }
            } finally {
                this.queueWriteLock.unlock();
            }
            trialCounter++;
        } while (tracerCounter < this.queueConfig.size && trialCounter < TRIALS);

        return success;
    }

    /**
     * Returns the QueueTracer for the current thread. If no one was found a QueueNullTracer will be returned.
     *
     * @return the QueueTracer for the current thread
     */
    public QueueTracer<?> getCurrentQueueTracer() {
        this.queueReadLock.lock();
        try {
            QueueTracer<?> tracer;
            if (this.queueConfig.enabled) {
//        tracer = this.queueConfig.tracerMap.get(Thread.currentThread());
                tracer = this.queueConfig.currentTracer.get();
                if (tracer == null) {
                    tracer = this.queueConfig.queueNullTracer;
                }
            } else {
                tracer = this.queueConfig.queueNullTracer;
            }

            return tracer;
        } finally {
            this.queueReadLock.unlock();
        }
    }
}
