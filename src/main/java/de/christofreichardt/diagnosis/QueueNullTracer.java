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
