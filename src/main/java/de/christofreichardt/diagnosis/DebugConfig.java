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
 * Helper class which contains the trace options of a tracer for a particular thread. For internal use
 * primarily.
 *
 * @author Christof Reichardt
 */
public class DebugConfig {

    private final boolean online;
    private final int level;

    /**
     * Constructor expects the trace options.
     *
     * @param online indicates if output is wanted
     * @param level  the trace depth
     */
    public DebugConfig(boolean online, int level) {
        this.online = online;
        this.level = level;
    }

    /**
     * online getter.
     *
     * @return a boolean value which indicates if any trace output is to be written
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * level getter.
     *
     * @return the trace depth
     */
    public int getLevel() {
        return level;
    }
}
