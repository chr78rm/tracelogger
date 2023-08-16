/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

        File logDir = new File(substitute((String) xpath.evaluate("./dns:LogDir/text()", node, XPathConstants.STRING)));
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
     * Checks if the file size limit has been exceeded and splits the trace file if need be.
     */
    protected void checkLimit() {
        synchronized (this.getSyncObject()) {
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
