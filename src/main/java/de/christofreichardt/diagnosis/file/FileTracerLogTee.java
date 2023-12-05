/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.DebugLogTee;
import java.nio.file.Path;

/**
 * A DebugLogTee that uses a {@link de.christofreichardt.diagnosis.file.FileTracer FileTracer} internally.
 *
 * @author Christof Reichardt
 */
abstract public class FileTracerLogTee extends DebugLogTee<FileTracer> {

    /**
     * Constructs a {@code FileTracerLogTee} using the given name for the embedded {@link FileTracer}.
     * @param name the name of the embedded {@link FileTracer}
     */
    public FileTracerLogTee(String name) {
        super(name, new FileTracer(name));
    }

    /**
     * Returns the configured log directory for the embedded {@link FileTracer}.
     *
     * @return the configured log directory
     */
    public Path getLogDirPath() {
        return super.tracer.getLogDirPath();
    }

    /**
     * Setter to override the default log directory of the embedded {@link FileTracer}.
     * @param logDirPath the desired log directory
     */
    public void setLogDirPath(Path logDirPath) {
        if (!logDirPath.toFile().isDirectory()) {
            throw new IllegalArgumentException("Need a path to a directory.");
        }

        super.tracer.setLogDirPath(logDirPath);
    }
}
