/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
