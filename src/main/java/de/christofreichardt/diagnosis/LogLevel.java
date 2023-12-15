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
 * Enumeration of the pre-defined log level.
 *
 * @author Christof Reichardt
 */
public enum LogLevel {

    /**
     * indicates an information message.
     */
    INFO,
    /**
     * indicates a warning message.
     */
    WARNING,
    /**
     * indicates an error message.
     */
    ERROR,
    /**
     * indicates a more serious error message.
     */
    FATAL,
    /**
     * indicates a severe error message.
     */
    SEVERE
}
