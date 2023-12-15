/*
 * Copyright 2014-2023 Christof Reichardt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
