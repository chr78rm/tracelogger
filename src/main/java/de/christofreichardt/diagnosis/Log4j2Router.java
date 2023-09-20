/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.diagnosis;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;

/**
 * @author developer
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

    @Override
    public void logMessage(LogLevel logLevel, String message, Class<?> clazz, String methodName) {
        ExtendedLogger extendedLogger = (ExtendedLogger) LogManager.getLogger(clazz);
        extendedLogger.logMessage(Log4j2Router.class.getName(), convertToLogbackLevel(logLevel), null, new SimpleMessage(message), null);
    }

    @Override
    public void logException(LogLevel logLevel, Throwable throwable, Class<?> clazz, String methodName) {
        ExtendedLogger extendedLogger = (ExtendedLogger) LogManager.getLogger(clazz);
        extendedLogger.logMessage(Log4j2Router.class.getName(), convertToLogbackLevel(logLevel), null, new SimpleMessage("Catched: "), throwable);
    }
}
