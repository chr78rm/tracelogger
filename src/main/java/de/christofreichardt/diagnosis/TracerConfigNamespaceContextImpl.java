/*
 * TracerConfigNamespaceContextImpl.java
 */

package de.christofreichardt.diagnosis;

import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.namespace.NamespaceContext;

/**
 * Since the TracerFactory uses XPath to access the XML configuration involving namespaces an implementation of the
 * NamespaceContext interface is necessary. The implementation is simplified as only the namespace "http://www.christofreichardt.de/java/tracer"
 * and the prefix "dns" will be considered.
 *
 * @author Christof Reichardt
 */
public class TracerConfigNamespaceContextImpl implements NamespaceContext {

    /* =========================
     * inner classes
     * ========================= */

    /**
     * Simplified implementation of an iterator.
     */
    public class Iterator implements java.util.Iterator<String> {
        boolean flag = true;

        /**
         * Returns true if and only if next() hasn't been called yet.
         *
         * @return true when called once then false.
         */
        @Override
        public boolean hasNext() {
            return this.flag;
        }

        /**
         * Unsupported operation.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns the next prefix, that is the first call will return "dns" and then a NoSuchElementException will be raised.
         *
         * @return the next prefix
         */
        @Override
        public String next() {
          if (hasNext() == false) {
            throw new NoSuchElementException();
          }

            this.flag = false;

            return "dns";
        }
    }

    /* =========================
     * constructors
     * ========================= */

    /**
     * default constructor.
     */
    public TracerConfigNamespaceContextImpl() {
    }

    /* =========================
     * properties
     * ========================= */

    /**
     * Returns the fixed namespace URI for the prefix "dns". Other prefixes will cause an IllegalArgumentException.
     *
     * @param prefix the namespace prefix
     * @return the URI "http://www.christofreichardt.de/java/tracer"
     */
    @Override
    public String getNamespaceURI(String prefix) {
      if (prefix == null || !prefix.equals("dns")) {
        throw new IllegalArgumentException("Accept only default namespace.");
      }

        return "http://www.christofreichardt.de/java/tracer";
    }

    /**
     * Returns the prefix for the fixed namespace "http://www.christofreichardt.de/java/tracer". Other namespaces will cause
     * an IllegalArgumentException.
     *
     * @param namespaceURI the namespace URI
     * @return the prefix "dns"
     */
    @Override
    public String getPrefix(String namespaceURI) {
      if (namespaceURI == null || !namespaceURI.equals("http://www.christofreichardt.de/java/tracer")) {
        throw new IllegalArgumentException("Accept only default namespace.");
      }

        return "dns";
    }

    /**
     * Returns a special iterator implementation.
     *
     * @param namespaceURI the namespace URI
     * @return an iterator
     */
    @Override
    public Iterator getPrefixes(String namespaceURI) {
        return new TracerConfigNamespaceContextImpl.Iterator();
    }

}
