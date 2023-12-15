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

package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.io.IndentablePrintStream;
import de.christofreichardt.diagnosis.io.TracePrintStream;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 * This particular tracer logs into the local file system.
 *
 * @author Christof Reichardt
 */
public class FileTracer extends AbstractTracer {

    /**
     * the actual log file
     */
    protected File traceLogfile;
    /**
     * the underlying OutputStream
     */
    protected FileOutputStream fileOutputStream;
    /**
     * indicates the lower bound of the number of bytes which leads to a log file rotation
     */
    volatile protected long byteLimit = -1;
    /**
     * denotes default log directory
     */
    protected Path logDirPath = FileSystems.getDefault().getPath("log");
    /**
     * counts the number of file splittings
     */
    protected int counter = -1;

    /**
     * Constructor expects the preferably unique tracer name. This is at the same time the name of the logfile.
     *
     * @param name the name of the tracer
     */
    public FileTracer(String name) {
        super(name);
    }

    /**
     * Gives the path to the directory of the logfile.
     *
     * @return the logDirPath
     */
    public Path getLogDirPath() {
        return logDirPath;
    }

    /**
     * Sets the path to the directory of the logfile.
     *
     * @param logDirPath the logDirPath to set
     */
    public void setLogDirPath(Path logDirPath) {
      if (!logDirPath.toFile().isDirectory()) {
        throw new IllegalArgumentException("Need a path to a directory.");
      }

        this.logDirPath = logDirPath;
    }

    /**
     * Indicates the lower bound of the number of bytes which leads to a log file rotation.
     *
     * @return the byteLimit
     */
    public long getByteLimit() {
        return byteLimit;
    }

    /**
     * Sets the lower bound of the number of bytes which leads to a log file rotation.
     *
     * @param byteLimit the byteLimit to set
     */
    public void setByteLimit(long byteLimit) {
        this.byteLimit = byteLimit;
    }

    /**
     * Creates the underlying trace file and opens the associated trace streams. The file name will be assembled by the path to
     * log directory and the name of the tracer.
     */
    @Override
    public void open() {
        try {
            if (!this.isOpened()) {
                Path logFilePath = this.logDirPath.resolve(String.format("%s.log", super.getName()));

                System.out.printf("%s Opening [%s] ...%n", formatVersionInfo(), logFilePath.toAbsolutePath());

                this.traceLogfile = logFilePath.toFile();
                this.fileOutputStream = new FileOutputStream(this.traceLogfile);
                this.setBufferedOutputStream(new BufferedOutputStream(this.fileOutputStream, this.getBufferSize()));
                this.setTracePrintStream(new TracePrintStream(this.getBufferedOutputStream(), this.getThreadMap()));

                this.getTracePrintStream().printf("--> TraceLog opened!%n");
                this.getTracePrintStream().printf("    Time     : %s%n", ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
                this.getTracePrintStream().printf("    Bufsize  : %d%n", this.getBufferSize());
                this.getTracePrintStream().printf("    Autoflush: %b%n%n", this.isAutoflush());
                this.setOpened(true);
            } else {
                System.err.println("WARNING: Tracelog is opened already.");
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Closes the associated trace streams.
     */
    @Override
    public void close() {
        try {
            if (this.isOpened()) {
                this.getTracePrintStream().println();
                this.getTracePrintStream().printf("--> TraceLog closing!%n");
                this.getTracePrintStream().printf("    Time     : %s%n", ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));

                System.out.printf("%s Closing [%s] ...%n", formatStreamErrorState(), this.traceLogfile.toPath().toAbsolutePath());

                this.getTracePrintStream().close();
                this.getBufferedOutputStream().close();
                this.fileOutputStream.close();
                this.setOpened(false);
            } else {
                System.err.println("WARNING: Tracelog is closed already.");
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    protected void readConfiguration(XPath xpath, Node node) throws XPathExpressionException, FileTracer.Exception {
        super.readConfiguration(xpath, node);

        File logDir = new File((String) xpath.evaluate("./dns:LogDir/text()", node, XPathConstants.STRING));
        if (!logDir.isDirectory()) {
            throw new FileTracer.Exception("Invalid path to directory configured for tracer: " + super.getName());
        }
        this.logDirPath = logDir.toPath();

        String strLimit = (String) xpath.evaluate("./dns:Limit/text()", node, XPathConstants.STRING);
        if (!strLimit.isEmpty()) {
            this.byteLimit = Long.parseLong(strLimit);
        } else {
            this.byteLimit = -1;
        }

        System.out.println("this.logDir = " + this.logDirPath);
        System.out.println("this.byteLimit = " + this.byteLimit);
    }

    /**
     * A helper object used to facilitate the roll-over of a {@code Lock} from one instance of a {@link TracePrintStream} to another instance.
     */
    protected TracePrintStream.LockAccess lockAccess;

    /**
     * The {@link TracePrintStream} uses this method to grant lock access to this {@code FileTracer}.
     * @param lockAccess a helper object with access to the employed {@code Lock} of the enclosing {@link TracePrintStream}
     */
    public void requestLockAccess(TracePrintStream.LockAccess lockAccess) {
        this.lockAccess = lockAccess;
    }

    /**
     * Checks if the file size limit has been exceeded and splits the trace file if need be.
     */
    protected void checkLimit() {
        this.getTracePrintStream().lock();
        this.getTracePrintStream().grantLockAccess(this);
        ReentrantLock reentrantLock = this.lockAccess.getLock();
        try {
            if (this.byteLimit != -1 && this.traceLogfile != null && this.traceLogfile.length() > this.byteLimit) {
                close();

                int pos = this.traceLogfile.getName().lastIndexOf('.');
                String splitFilename = String.format("%s.%d.log", this.traceLogfile.getName().substring(0, pos), ++this.counter);
                try {
                    Files.move(this.traceLogfile.toPath(), this.logDirPath.resolve(splitFilename), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }

                open();
            }
        } finally {
            this.getTracePrintStream().grantLockAccess(this);
            this.lockAccess.setLock(reentrantLock);
            this.getTracePrintStream().unlock();
        }
    }

    @Override
    public IndentablePrintStream out() {
        checkLimit();
        return super.out();
    }

    @Override
    public void logException(LogLevel logLevel, Throwable throwable, Class<?> clazz, String methodName) {
        super.logException(logLevel, throwable, clazz, methodName);
        checkLimit();
    }

    @Override
    public void logMessage(LogLevel logLevel, String message, Class<?> clazz, String methodName) {
        super.logMessage(logLevel, message, clazz, methodName);
        checkLimit();
    }
}
