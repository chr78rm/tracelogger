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

/**
 * <div style="text-align: justify">
 * Provides the basic tracing faclitities. The most important class is {@link de.christofreichardt.diagnosis.AbstractTracer AbstractTracer} which is the root
 * of the tracer hierarchy. The {@link de.christofreichardt.diagnosis.TracerFactory TracerFactory} can be feed with an appropriate configuration and thereupon can be used
 * to access tracers by name, by thread or from a blocking, bounded queue.
 * </div>
 */
package de.christofreichardt.diagnosis;
