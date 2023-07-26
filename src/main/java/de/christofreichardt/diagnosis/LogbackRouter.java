/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import ch.qos.logback.classic.Level;
import javax.xml.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
import org.w3c.dom.Node;

/**
 *
 * @author Christof Reichardt
 */
final public class LogbackRouter extends NullTracer {
  
  private int convertToLogbackLevel(LogLevel logLevel) {
    Level level = Level.OFF;
    
    switch(logLevel) {
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

  @Override
  public void logMessage(LogLevel logLevel, String message, Class<?> clazz, String methodName) {
    LocationAwareLogger logger = (LocationAwareLogger) LoggerFactory.getLogger(clazz);
    logger.log(null, LogbackRouter.class.getName(), convertToLogbackLevel(logLevel), message, null, null);
  }

  @Override
  public void logException(LogLevel logLevel, Throwable throwable, Class<?> clazz, String methodName) {
    LocationAwareLogger logger = (LocationAwareLogger) LoggerFactory.getLogger(clazz);
    logger.log(null, LogbackRouter.class.getName(), convertToLogbackLevel(logLevel), throwable.getMessage(), null, throwable);
  }

  @Override
  protected void readConfiguration(XPath xpath, Node node) {
    Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    if (!logger.getClass().getName().equals("ch.qos.logback.classic.Logger"))
      throw new IllegalArgumentException("Logback isn't bound.");
  }

  
}
