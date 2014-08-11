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
   * @throws java.io.IOException
   */
  @Override
  public void write(int b) throws IOException {
  }

  /**
   * Pseudo write()-method.
   * 
   * @param b
   * @param off
   * @param len
   * @throws IOException 
   */
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
  }

  /**
   * Pseudo write()-method.
   * 
   * @param b
   * @throws IOException 
   */
  @Override
  public void write(byte[] b) throws IOException {
  }
}
