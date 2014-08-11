/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.diagnosis;

import de.christofreichardt.diagnosis.file.FileTracerUnit;
import de.christofreichardt.diagnosis.file.JDKLoggerUnit;
import de.christofreichardt.diagnosis.file.Log4jUnit;
import de.christofreichardt.diagnosis.file.QueueFileTracerUnit;
import de.christofreichardt.diagnosis.net.NetTracerUnit;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Developer
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TracerFactoryUnit.class,
  FileTracerUnit.class,
  NetTracerUnit.class,
  Log4jUnit.class,
  JDKLoggerUnit.class,
  LoadUnit.class,
  QueueFileTracerUnit.class,
  ExampleUnit.class,
  ExperimentalUnit.class})
public class MyTestSuite {
}
