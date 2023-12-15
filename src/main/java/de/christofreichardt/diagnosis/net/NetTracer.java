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

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.io.TracePrintStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 * This particular tracer logs over the network.
 *
 * @author Christof Reichardt
 */
public class NetTracer extends AbstractTracer {

    /**
     * The timeout when connecting to the remote logging server in milliseconds
     */
    public static final int TIMEOUT = 5000;

    /**
     * the debug server's port number
     */
    protected int portNo;
    /**
     * the host name of the debug server
     */
    protected String hostName = "localhost";
    /**
     * the pizza connection to the debug server
     */
    protected Socket pizzaConnection = null;

    /**
     * Constructor expects the name of the tracer. That name will be sent over the network when connecting to remote logging server.
     *
     * @param name the name of the tracer
     */
    public NetTracer(String name) {
        super(name);
    }

    /**
     * Returns the port number used to connect to the remote logging server.
     *
     * @return the portNo
     */
    public int getPortNo() {
        return portNo;
    }

    /**
     * Sets the port number used to connect to the remote logging server.
     *
     * @param portNo the portNo to set
     */
    public void setPortNo(int portNo) {
        this.portNo = portNo;
    }

    /**
     * Returns the hostname used to connect to the remote logging server.
     *
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Sets the hostname used to connect to the remote logging server.
     *
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
    protected void readConfiguration(XPath xpath, Node node) throws XPathExpressionException, Exception {
        super.readConfiguration(xpath, node);

        this.hostName = (String) xpath.evaluate("./dns:Listener/dns:Host/text()", node, XPathConstants.STRING);
        this.portNo = Integer.parseInt((String) xpath.evaluate("./dns:Listener/dns:Port/text()", node, XPathConstants.STRING));

        System.out.println("this.hostName = " + this.hostName);
        System.out.println("this.portNo = " + this.portNo);
    }

    /**
     * Opens the associated {@link TracePrintStream} by wrapping the socket streams.
     */
    @Override
    public void open() {
        try {
            if (!this.isOpened()) {
                System.out.printf("%s Opening [%s, %d] ...%n", formatVersionInfo(), this.hostName, this.portNo);

                InetSocketAddress inetSocketAddress = new InetSocketAddress(this.hostName, this.portNo);
                this.pizzaConnection = new Socket();
                this.pizzaConnection.connect(inetSocketAddress, TIMEOUT);
                this.setBufferedOutputStream(new BufferedOutputStream(this.pizzaConnection.getOutputStream(), this.getBufferSize()));
                this.setTracePrintStream(new TracePrintStream(this.getBufferedOutputStream(), this.getThreadMap()));

                sendUserCredentials();
                this.getTracePrintStream().printf("--> TraceLog opened!%n");
                this.getTracePrintStream().printf("    Time     : %tc%n", new Date());
                this.getTracePrintStream().printf("    Bufsize  : %d%n", this.getBufferSize());
                this.getTracePrintStream().printf("    Autoflush: %b%n%n", this.isAutoflush());

                this.setOpened(true);
            } else {
                System.err.println("WARNING: Tracelog is opened already.");
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Closes the associated {@link TracePrintStream}.
     */
    @Override
    public void close() {
        try {
            if (this.isOpened()) {
                this.getTracePrintStream().println();
                this.getTracePrintStream().printf("--> TraceLog closing!%n");
                this.getTracePrintStream().printf("    Time     : %tc%n", new Date());

                System.out.printf("%s Closing [%s, %d] ...%n", formatStreamErrorState(), this.hostName, this.portNo);

                this.getTracePrintStream().close();
                this.getBufferedOutputStream().close();
                this.pizzaConnection.close();

                this.setOpened(false);
            } else {
                System.err.println("WARNING: Tracelog is closed already.");
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Collects some user credentials and sends them over the network.
     *
     * @throws java.net.UnknownHostException if the local host name could not be resolved into an address
     */
    protected void sendUserCredentials() throws UnknownHostException {
        this.getTracePrintStream().printf("user = %s, host = %s, name = %s%n", System.getProperty("user.name"), InetAddress.getLocalHost().getHostName(), super.getName());
    }
}
