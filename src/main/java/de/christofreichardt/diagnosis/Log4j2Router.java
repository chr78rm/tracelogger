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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;

/**
 * A specialized {@link NullTracer} which delegates log messages to Log4j2 loggers. For more information about Log4j2, see
 * <a href="https://logging.apache.org/log4j/2.x/">Apache Log4j</a>.
 *
 * @author Christof Reichardt
 */
public class Log4j2Router extends NullTracer {

    private Level convertToLogbackLevel(LogLevel logLevel) {
        Level level = Level.OFF;
        switch (logLevel) {
            case INFO:
                level = Level.INFO;
                break;
            case WARNING:
                level = Level.WARN;
                break;
            case ERROR:
            case FATAL:
            case SEVERE:
                level = Level.ERROR;
                break;
        }

        return level;
    }

    /**
     * This method gets itself a Log4j2 <a href="https://logging.apache.org/log4j/2.x/javadoc/log4j-api/org/apache/logging/log4j/spi/ExtendedLogger.html">ExtendedLogger</a> by invoking Log4j2s
     * LogManager with the given clazz. Thereupon the {@code ExtendedLogger} will be used to log the given message.
     *
     * @param logLevel will be converted to Log4j2s level
     * @param message  the to be logged message
     * @param clazz    the originating class
     * @param methodName the originating method
     */
    @Override
    public void logMessage(LogLevel logLevel, String message, Class<?> clazz, String methodName) {
        ExtendedLogger extendedLogger = (ExtendedLogger) LogManager.getLogger(clazz);
        extendedLogger.logMessage(Log4j2Router.class.getName(), convertToLogbackLevel(logLevel), null, new SimpleMessage(message), null);
    }

    /**
     * This method gets itself a Log4j2 <a href="https://logging.apache.org/log4j/2.x/javadoc/log4j-api/org/apache/logging/log4j/spi/ExtendedLogger.html">ExtendedLogger</a> by invoking Log4j2s
     * LogManager with the given clazz. Thereupon the {@code ExtendedLogger} will be used to log the given throwable.
     *
     * @param logLevel  will be converted to Log4j2s level
     * @param throwable the to be logged throwable
     * @param clazz     the originating class
     * @param methodName the name of the relevant method
     */
    @Override
    public void logException(LogLevel logLevel, Throwable throwable, Class<?> clazz, String methodName) {
        ExtendedLogger extendedLogger = (ExtendedLogger) LogManager.getLogger(clazz);
        extendedLogger.logMessage(Log4j2Router.class.getName(), convertToLogbackLevel(logLevel), null, new SimpleMessage("Catched: "), throwable);
    }
}
