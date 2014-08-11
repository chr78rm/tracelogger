/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.QueueTracer;
import java.io.File;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 * A {@link QueueTracer} that uses a {@link FileTracer} internally.
 * 
 * @author Christof Reichardt
 */
public class QueueFileTracer extends QueueTracer<FileTracer> {

  public QueueFileTracer(String name) {
    super(name, new FileTracer(name));
  }

  @Override
  protected void readConfiguration(XPath xpath, Node node) throws XPathExpressionException, QueueFileTracer.Exception {
    super.readConfiguration(xpath, node);
    File logDir = new File(substitute((String) xpath.evaluate("./dns:TraceLogger/dns:LogDir/text()", node, XPathConstants.STRING)));
    super.tracer.setLogDirPath(logDir.toPath());
  }

}
