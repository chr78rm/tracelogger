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
