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
 * Classes which implement this interface delegate the retrieval of a particular tracer to the
 * specified method. This can be used to override the configuration of the {@link TracerFactory} for
 * certain classes.
 *
 * @author Christof Reichardt
 */
public interface Traceable {
    /**
     * Returns the "current" tracer for the implementing class. Most of the time it might delegate to {@link TracerFactory#getCurrentPoolTracer()},
     * {@link TracerFactory#getCurrentQueueTracer()} or {@link TracerFactory#getDefaultTracer()}.
     *
     * @return the "current" tracer for the implementing class
     */
    AbstractTracer getCurrentTracer();
}
