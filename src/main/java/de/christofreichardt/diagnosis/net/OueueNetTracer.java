/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis.net;

import de.christofreichardt.diagnosis.QueueTracer;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 * Not supported yet.
 *
 * @author Christof Reichardt
 */
public class OueueNetTracer extends QueueTracer<NetTracer> {

    public OueueNetTracer(String name, NetTracer tracer) { // TODO: the signature needs to be changed, the code responsible for configuration expects a single argument constructor of type String
        super(name, new NetTracer(name));
    }

    @Override
    protected void readConfiguration(XPath xpath, Node node) throws XPathExpressionException, OueueNetTracer.Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
