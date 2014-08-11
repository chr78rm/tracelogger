/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 *
 * @author Developer
 */
public class Measure implements TestRule {
  
  final private int warmup;
  final private int measurements;
  final private long[] runTimes;
  private int current;
  private long totalRunTime = 0;
  private double meanTime;
  private double standardDeviation;

  public Measure(int repetitions, int measurements) {
    this.warmup = repetitions;
    this.measurements = measurements;
    this.runTimes = new long[this.measurements];
  }

  public int getWarmup() {
    return warmup;
  }

  public int getMeasurements() {
    return measurements;
  }

  public int getCurrent() {
    return current;
  }

  public boolean isLastRun() {
    return this.current == this.measurements - 1;
  }

  public double getMeanTime() {
    return meanTime;
  }

  public double getStandardDeviation() {
    return standardDeviation;
  }

  public long getTotalRunTime() {
    return totalRunTime;
  }

  @Override
  public Statement apply(final Statement baseStatement, Description description) {

    try {
      
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {

          try {
            for (int i = 0; i < Measure.this.warmup; i++) {
              baseStatement.evaluate();
            }
            
            
            for (int i = 0; i < Measure.this.measurements; i++) {
              long startTime = System.nanoTime();
              baseStatement.evaluate();
              long endTime = System.nanoTime();
              long runTime = (endTime - startTime);
              
              Measure.this.runTimes[i] = runTime;
              Measure.this.current++;
              Measure.this.totalRunTime += runTime;
            }
            
            Measure.this.meanTime = Measure.this.getTotalRunTime()/(Measure.this.measurements);
            
            double sum = 0.0f;
            for (long runTime : Measure.this.runTimes) {
              sum += (runTime - Measure.this.getMeanTime())*(runTime - Measure.this.getMeanTime());
            }
            Measure.this.standardDeviation = StrictMath.sqrt(sum/(Measure.this.measurements - 1));
            
            System.out.printf("----------------------------------------------------------------------------%n");
            System.out.printf("Measurements = %d%n", Measure.this.getMeasurements());
            System.out.printf("TotalRunTime = %gs%n", Measure.this.getTotalRunTime()/1e+9f);
            System.out.printf("MeanTime = %gs%n", Measure.this.getMeanTime()/1e+9f);
            System.out.printf("StandardDeviation = %gs%n", Measure.this.getStandardDeviation()/1e+9f);
            System.out.printf("%g%% deviation of the meantime%n", Measure.this.getStandardDeviation()/(Measure.this.getMeanTime()/(100)));
            System.out.printf("----------------------------------------------------------------------------%n");
          }
          finally {
          }
        }
      };
    }
    finally {
    }
  }
  
}
