/*
 * Copyright 2018 Christof Reichardt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.christofreichardt.diagnosis;

import ch.qos.logback.core.joran.spi.JoranException;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christof Reichardt
 */
public class Log4j2RouterUnit {
  public static final String PATH_TO_LOGDIR = "." + File.separator + "log" + File.separator + "log4j2";
  public static final String PATH_TO_LOGFILE = PATH_TO_LOGDIR + File.separator + "log4j2-test.log";
  
  final private BannerPrinter bannerPrinter = new BannerPrinter();
  
  
  @Before
  public void setup() throws IOException, JoranException {
    File logDir = new File(PATH_TO_LOGDIR);
    File[] logFiles = logDir.listFiles((File file) -> file.getName().endsWith("log") && !"empty.log".equals(file.getName()));
    for (File logFile : logFiles) {
      System.out.printf("Deleting '%s'.%n", logFile.getName());
      if (!logFile.delete()) throw new IOException("Cannot delete '" + logFile.getAbsolutePath() + "'.");
    }
  }
  
  @Test
  public void loggingRouter() {
    this.bannerPrinter.start("loggingRouter", getClass());
  }
}
