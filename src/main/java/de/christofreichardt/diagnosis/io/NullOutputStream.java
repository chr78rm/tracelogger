/*
 * NullOutputStream.java
 */
package de.christofreichardt.diagnosis.io;

import java.io.OutputStream;
import java.io.IOException;

/**
 * This OutputStream discards all bytes that are written into it.
 *
 * @author Christof Reichardt
 */
final public class NullOutputStream extends OutputStream {

  /**
   * default constructor.
   */
  public NullOutputStream() {
    super();
  }

  /**
   * Pseudo write()-method.
   *
   * @param b won't be evaluated
   */
  @Override
  public void write(int b) {
  }

  /**
   * Pseudo write()-method.
   * 
   * @param b won't be evaluated
   * @param off won't be evaluated
   * @param len won't be evaluated
   */
  @Override
  public void write(byte[] b, int off, int len) {
  }

  /**
   * Pseudo write()-method.
   * 
   * @param b won't be evaluated
   */
  @Override
  public void write(byte[] b) {
  }
}
