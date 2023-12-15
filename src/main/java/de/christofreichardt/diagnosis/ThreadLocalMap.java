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

import java.util.NoSuchElementException;

/**
 * A {@link AbstractThreadMap} which maps threads on {@link TracingContext}s by using <code>ThreadLocal</code>s.
 *
 * @author Christof Reichardt
 */
public class ThreadLocalMap extends AbstractThreadMap {
    private final ThreadLocal<TracingContext> currentTracingContext = new ThreadLocal<>();

    @Override
    public int getCurrentStackSize() {
        int stackSize;
        if (this.currentTracingContext.get() != null) {
            stackSize = this.currentTracingContext.get().getMethodStack().size();
        } else {
            stackSize = -1;
        }

        return stackSize;
    }

    @Override
    TracingContext getCurrentTracingContext() {
        return this.currentTracingContext.get();
    }

    @Override
    void setCurrentTracingContext(TracingContext tracingContext) {
        this.currentTracingContext.set(tracingContext);
    }

    @Override
    TracingContext removeCurrentTracingContext() {
        TracingContext tracingContext = this.currentTracingContext.get();
        this.currentTracingContext.remove();
        return tracingContext;
    }

    @Override
    boolean push(TraceMethod traceMethod) {
        boolean success;
        if (this.currentTracingContext.get() != null && !this.currentTracingContext.get().isCorrupted()) {
            if (this.currentTracingContext.get().getMethodStack().size() >= STACK_SIZE) {
                this.currentTracingContext.get().setCorrupted(true);
                throw new ThreadLocalMap.RuntimeException("Stacksize is exceeded.");
            } else {
                this.currentTracingContext.get().getMethodStack().push(traceMethod);
                success = true;
            }
        } else {
            success = false;
        }

        return success;
    }

    @Override
    TraceMethod pop() {
        TraceMethod traceMethod = null;
        if (this.currentTracingContext.get() != null && !this.currentTracingContext.get().isCorrupted()) {
            try {
                traceMethod = this.currentTracingContext.get().getMethodStack().pop();
                traceMethod.stopTime();
            } catch (NoSuchElementException ex) {
                this.currentTracingContext.get().setCorrupted(true);
                throw new ThreadLocalMap.RuntimeException(ex);
            }
        }

        return traceMethod;
    }
}
