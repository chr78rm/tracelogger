/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

/**
 * <div style="text-align: justify">
 * This is the tracer the {@link TracerFactory} delivers if the Queue is disabled. To redirect log messages
 * to the JDK logging system use the {@link JDKLoggingRouter} as constructor argument. Note that while this is
 * assignable to a QueueTracer it must not be configured as an actual tracer type for the queue. Instead disable the Queue
 * and you will get by default and non-blocking QueueNullTracer instances.
 * </div>
 *
 * @author Christof Reichardt
 */
final public class QueueNullTracer extends QueueTracer<NullTracer> {

    /**
     * The constructor expects a {@link NullTracer}.
     *
     * @param tracer the to be wrapped tracer
     */
    public QueueNullTracer(NullTracer tracer) {
        super("__NullTracer__", tracer);
    }

}
