/*
 * DebugConfig.java
 */
package de.christofreichardt.diagnosis;

/**
 * Helper class which contains the trace options of a tracer for a particular thread. For internal use
 * primarily.
 *
 * @author Christof Reichardt
 */
public class DebugConfig {

  private final boolean online;
  private final int level;

  /**
   * Constructor expects the trace options.
   *
   * @param online indicates if output is wanted
   * @param level the trace depth
   */
  public DebugConfig(boolean online, int level) {
    this.online = online;
    this.level = level;
  }

  /**
   * online getter.
   *
   * @return a boolean value which indicates if any trace output is to be written
   */
  public boolean isOnline() {
    return online;
  }

  /**
   * level getter.
   *
   * @return the trace depth
   */
  public int getLevel() {
    return level;
  }
}
