/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.DebugLogTee;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.TraceMethod;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Connects to the log4j system and uses additionally an internal {@link de.christofreichardt.diagnosis.file.FileTracer FileTracer}.
 * @author Christof Reichardt
 */
final public class FileTracerLog4jTee extends FileTracerLogTee {

  public FileTracerLog4jTee(String name) {
    super(name);
  }
  
  /**
   * Translates the native {@link de.christofreichardt.diagnosis.LogLevel LogLevel} to a log4j level.
   * 
   * @param logLevel the desired level
   * @return the translated level
   */
  protected Level convertToLog4jLevel(LogLevel logLevel) {
    Level level = Level.OFF;

    switch (logLevel) {
      case INFO:
        level = Level.INFO;
        break;
      case WARNING:
        level = Level.WARN;
        break;
      case ERROR:
        level = Level.ERROR;
        break;
      case FATAL:
      case SEVERE:
        level = Level.FATAL;
        break;
      default:
        break;
    }

    return level;
  }
  
  /**
   * Routes the given information to the log4j system.
   * 
   * @param logLevel {@inheritDoc}
   * @param message {@inheritDoc}
   * @param clazz {@inheritDoc} 
   */
  @Override
  protected void adapt(LogLevel logLevel, String message, Class clazz)
  {
    Logger logger = Logger.getLogger(clazz);
    Level level = convertToLog4jLevel(logLevel);
    logger.log(DebugLogTee.class.getName(), level, message, null);
  }

  /**
   * Routes the given information to the log4j system.
   * 
   * @param logLevel {@inheritDoc}
   * @param throwable {@inheritDoc}
   * @param clazz {@inheritDoc}
   */
  @Override
  protected void adapt(LogLevel logLevel, Throwable throwable, Class clazz)
  {
    Logger logger = Logger.getLogger(clazz);
    Level level = convertToLog4jLevel(logLevel);
    logger.log(DebugLogTee.class.getName(), level, throwable.getMessage(), throwable);
  }
  
  /**
   * Calls the base implementation and routes to the log4j system.
   * 
   * @param methodSignature {@inheritDoc}
   * @return {@inheritDoc}
   */
  @Deprecated
  @Override
  public TraceMethod entry(String methodSignature)
  {
    TraceMethod traceMethod = super.entry(methodSignature);

    if (traceMethod != null) {
      String className = "STATIC";
      Logger logger = Logger.getLogger(className);
      logger.log(getClass().getName(), Level.DEBUG, "ENTRY--" + methodSignature, null);
    }
    
    return traceMethod;
  }

  /**
   * Calls the base implementation and routes to the log4j system.
   * 
   * @param returnType {@inheritDoc}
   * @param object {@inheritDoc}
   * @param methodSignature {@inheritDoc}
   * @return {@inheritDoc}
   */
  @Override
  public TraceMethod entry(String returnType, Object object, String methodSignature)
  {
    TraceMethod traceMethod = super.entry(returnType, object, methodSignature);
    
    if (traceMethod != null) {
      String className = object != null ? object.getClass().getName() : "STATIC";
      Logger logger = Logger.getLogger(className);
      logger.log(getClass().getName(), Level.DEBUG, "ENTRY--" + methodSignature, null);
    }
    
    return traceMethod;
  }

  /**
   * Calls the base implementation and routes to the log4j system.
   * 
   * @param returnType {@inheritDoc}
   * @param clazz {@inheritDoc}
   * @param methodSignature {@inheritDoc}
   * @return {@inheritDoc} 
   */
  @Override
  public TraceMethod entry(String returnType, Class clazz, String methodSignature)
  {
    TraceMethod traceMethod = super.entry(returnType, clazz, methodSignature);
    
    if (traceMethod != null) {
      String className = clazz != null ? clazz.getName() : "STATIC";
      Logger logger = Logger.getLogger(className);
      logger.log(getClass().getName(), Level.DEBUG, "ENTRY--" + methodSignature, null);
    }
    
    return traceMethod;
  }

  /**
   * Calls the base implementation and routes to the log4j system.
   * 
   * @return {@inheritDoc}
   */
  @Override
  public TraceMethod wayout()
  {
    TraceMethod traceMethod = super.wayout();
    if (traceMethod != null)
    {
      Logger logger = traceMethod.getClazz() != null ? Logger.getLogger(traceMethod.getClazz().getName()) : Logger.getLogger("STATIC");
      logger.log(getClass().getName(), Level.DEBUG, "RETURN-" + traceMethod.getMethodName(), null);
    }
    
    return traceMethod;
  }
}
