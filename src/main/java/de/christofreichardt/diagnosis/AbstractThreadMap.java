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
 * Has been introduced to exchange thread maps which are based on <code>Map&lt;Thread,TracingContext&gt;</code> with
 * thread maps which are based on <code>ThreadLocal</code>s and vice versa.
 *
 * @author Christof Reichardt
 */
abstract public class AbstractThreadMap {

    /**
     * Indicates a corruption of the method stack.
     */
    static public class RuntimeException extends java.lang.RuntimeException {

        public RuntimeException(String msg) {
            super(msg);
        }

        public RuntimeException(Throwable cause) {
            super(cause);
        }
    }

    /** denotes the maximal number of traced methods on the stack */
    public static final int STACK_SIZE = 50;

    /**
     * Returns the stack size of the current thread. The value -1 indicates that the current thread isn't registered.
     *
     * @return the current stack size or -1 if there is no stack for the current thread
     */
    abstract public int getCurrentStackSize();

    abstract TracingContext getCurrentTracingContext();

    abstract void setCurrentTracingContext(TracingContext tracingContext);

    abstract TracingContext removeCurrentTracingContext();

    abstract boolean push(TraceMethod traceMethod);

    abstract TraceMethod pop();
}
