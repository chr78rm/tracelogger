/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.diagnosis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;

/**
 *
 * @author developer
 */
@Plugin(name = "MyXMLConfigurationFactory", category = "ConfigurationFactory")
@Order(10)
public class MyXMLConfigurationFactory extends XmlConfigurationFactory {

  @Override
  public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation) {
    File configFile = new File("." + File.separator + "config" + File.separator + "log4j2.xml");
    try {
      try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
        ConfigurationSource configurationSource = new ConfigurationSource(fileInputStream, configFile);
        return super.getConfiguration(loggerContext, configurationSource);
      }
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  @Override
  public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource configurationSource) {
    return getConfiguration(loggerContext, configurationSource.toString(), null);
  }

}
