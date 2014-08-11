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
 * @author Christof Reichardt
 */
abstract public class FileTracerLogTee extends DebugLogTee<FileTracer> {

  public FileTracerLogTee(String name) {
    super(name, new FileTracer(name));
  }

  /**
   * @return the logDirPath
   */
  public Path getLogDirPath() {
    return super.tracer.getLogDirPath();
  }

  /**
   * @param logDirPath the logDirPath to set
   */
  public void setLogDirPath(Path logDirPath) {
    if (!logDirPath.toFile().isDirectory())
      throw new IllegalArgumentException("Need a path to a directory.");
    
    super.tracer.setLogDirPath(logDirPath);
  }
}
