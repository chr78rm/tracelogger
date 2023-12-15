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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The tracing context of one particular thread. It manages the stack of the to be observed methods.
 * For internal use.
 *
 * @author Christof Reichardt
 */
public class TracingContext {

    private int debugLevel = -1;
    private boolean online = false;
    private final Deque<TraceMethod> methodStack = new ArrayDeque<>();
    private boolean corrupted = false;

    /**
     * Creates a new instance of TracingContext by evaluating the given parameter.
     *
     * @param debugLevel limites the part of the stack for which output will generated
     * @param online     if true output will generated until the denoted limit, otherwise no output will generated at all
     */
    TracingContext(int debugLevel, boolean online) {
        this.debugLevel = debugLevel;
        this.online = online;
    }

    /**
     * Creates a new instance of TracingContext by evaluating the given DebugConfig object.
     *
     * @param debugConfig a simple bean with debugLevel and online properties
     */
    TracingContext(DebugConfig debugConfig) {
        this.debugLevel = debugConfig.getLevel();
        this.online = debugConfig.isOnline();
    }

    /**
     * @return the debugLevel
     */
    int getDebugLevel() {
        return debugLevel;
    }

    /**
     * @param debugLevel the debugLevel to set
     */
    void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    /**
     * @return the online
     */
    boolean isOnline() {
        return online & !corrupted;
    }

    /**
     * @param online the online to set
     */
    void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * @return the methodStack
     */
    Deque<TraceMethod> getMethodStack() {
        return methodStack;
    }

    /**
     * Indicates that the method stack is corrupted, for example has overflowed.
     *
     * @return the corrupted
     */
    boolean isCorrupted() {
        return corrupted;
    }

    /**
     * @param corrupted the corrupted to set
     */
    void setCorrupted(boolean corrupted) {
        this.corrupted = corrupted;
    }
}
