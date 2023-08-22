/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.file.JDKLoggerUnit;
import de.christofreichardt.diagnosis.file.QueueFileTracerUnit;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Developer
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  LoadUnit.class,
  QueueFileTracerUnit.class,
  ExampleUnit.class,
  ExperimentalUnit.class,
  LogbackRouterUnit.class,
  Log4j2RouterUnit.class
})
public class MyTestSuite {
}
