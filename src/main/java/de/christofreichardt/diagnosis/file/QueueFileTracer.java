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

package de.christofreichardt.diagnosis.file;

import de.christofreichardt.diagnosis.QueueTracer;
import java.io.File;
import java.nio.file.Path;
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

    /**
     * Constructor expects the name of the (internal) {@link FileTracer}.
     * @param name the name of the tracer
     */
    public QueueFileTracer(String name) {
        super(name, new FileTracer(name));
    }

    /**
     * Returns the log directory of the internally used {@link FileTracer}.
     * @return the log directory
     */
    public Path getLogDirPath() {
        return super.tracer.getLogDirPath();
    }

    @Override
    protected void readConfiguration(XPath xpath, Node node) throws XPathExpressionException, QueueFileTracer.Exception {
        super.readConfiguration(xpath, node);
        File logDir = new File((String) xpath.evaluate("./dns:TraceLogger/dns:LogDir/text()", node, XPathConstants.STRING));
        super.tracer.setLogDirPath(logDir.toPath());
    }

}
