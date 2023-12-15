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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This specialised {@link NullTracer} redirects log messages to the core logging facilities of the Java platform,
 * see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.logging/java/util/logging/package-summary.html">java.util.logging</a>.
 * The {@link LogLevel}s will be translated into the {@link Level}s used by loggers of the Java platform. All of the other
 * tracing output will be discarded.
 *
 * @author Christof Reichardt
 */
final public class JDKLoggingRouter extends NullTracer {

    /**
     * Translates the {@link LogLevel}s into {@link Level}s.
     *
     * @param logLevel the to be translated {@link LogLevel}
     * @return the corresponding {@link Level}
     */
    protected Level convertToJDK14Level(LogLevel logLevel) {
        Level level = Level.OFF;

        switch (logLevel) {
            case INFO:
                level = Level.INFO;
                break;
            case WARNING:
                level = Level.WARNING;
                break;
            case ERROR:
            case FATAL:
            case SEVERE:
                level = Level.SEVERE;
                break;
        }

        return level;
    }

    /**
     * Calls the {@link Logger} for the given clazz.
     *
     * @param logLevel the log level
     * @param message  the to be logged message
     * @param clazz    the originating class
     */
    @Override
    public void logMessage(LogLevel logLevel, String message, Class<?> clazz, String methodName) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.logp(convertToJDK14Level(logLevel), clazz.getName(), methodName, message);
    }

    /**
     * Calls the {@link Logger} for the given clazz.
     *
     * @param logLevel  the log level
     * @param throwable the to be logged throwable
     * @param clazz     the originating class
     */
    @Override
    public void logException(LogLevel logLevel, Throwable throwable, Class<?> clazz, String methodName) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.logp(convertToJDK14Level(logLevel), clazz.getName(), "-", throwable.getMessage(), throwable);
    }

}
