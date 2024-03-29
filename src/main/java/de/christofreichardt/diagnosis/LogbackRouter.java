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

import ch.qos.logback.classic.Level;
import javax.xml.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
import org.w3c.dom.Node;

/**
 * A specialized {@link NullTracer} which delegates log messages to Logback loggers. For more information about Logback, see
 * <a href="https://logback.qos.ch/">Logback</a>.
 *
 * @author Christof Reichardt
 */
final public class LogbackRouter extends NullTracer {

    private int convertToLogbackLevel(LogLevel logLevel) {
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

        return Level.toLocationAwareLoggerInteger(level);
    }

    /**
     * This method gets itself a <a href="https://www.slf4j.org/apidocs/org/slf4j/spi/LocationAwareLogger.html">LocationAwareLogger</a> by invoking SLF4Js
     * <a href="https://www.slf4j.org/apidocs/org/slf4j/LoggerFactory.html">LoggerFactory</a> with the given clazz. Thereupon the {@code LocationAwareLogger} will be used
     * to log the given {@code message}.
     *
     * @param logLevel will be converted to an appropriate Logback level
     * @param message  the to be logged message
     * @param clazz    the originating class
     * @param methodName the originating method
     */
    @Override
    public void logMessage(LogLevel logLevel, String message, Class<?> clazz, String methodName) {
        LocationAwareLogger logger = (LocationAwareLogger) LoggerFactory.getLogger(clazz);
        logger.log(null, LogbackRouter.class.getName(), convertToLogbackLevel(logLevel), message, null, null);
    }

    /**
     * This method gets itself a <a href="https://www.slf4j.org/apidocs/org/slf4j/spi/LocationAwareLogger.html">LocationAwareLogger</a> by invoking SLF4Js
     * <a href="https://www.slf4j.org/apidocs/org/slf4j/LoggerFactory.html">LoggerFactory</a> with the given clazz. Thereupon the {@code LocationAwareLogger} will be used
     * to log the given {@code throwable}.
     *
     * @param logLevel  will be converted to an appropriate Logback level
     * @param throwable the to be logged throwable
     * @param clazz     the originating class
     * @param methodName the name of the relevant method
     */
    @Override
    public void logException(LogLevel logLevel, Throwable throwable, Class<?> clazz, String methodName) {
        LocationAwareLogger logger = (LocationAwareLogger) LoggerFactory.getLogger(clazz);
        logger.log(null, LogbackRouter.class.getName(), convertToLogbackLevel(logLevel), throwable.getMessage(), null, throwable);
    }

    @Override
    protected void readConfiguration(XPath xpath, Node node) {
        Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      if (!logger.getClass().getName().equals("ch.qos.logback.classic.Logger")) {
        throw new IllegalArgumentException("Logback isn't bound.");
      }
    }


}
