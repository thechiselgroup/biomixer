/*******************************************************************************
 * Copyright 2012 Lars Grammel, David Rusk 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout;

import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Ongoing computation of a layout on a specific set of nodes and edges.
 * 
 * @author Lars Grammel
 */
public interface LayoutComputation {

    /**
     * Adds an event handler that is notified when the computation is finished.
     * 
     * @return handler registration that can be used to de-register the event
     *         handler.
     */
    HandlerRegistration addEventHandler(LayoutComputationFinishedHandler handler);

    /**
     * @return <code>true</code> when the layout algorithm is still calculating
     *         the layout (e.g. if it is a continuous layout algorithm such as
     *         force directed layout).
     */
    boolean isRunning();

    /**
     * Forces the layout to compute another iteration. For a continuous layout,
     * this may revive the state of a computation that had finished but now has
     * new nodes.
     */
    void restart();

    /**
     * <p>
     * Stops the layout algorithm. Stopping the layout does not immediately stop
     * it, but it will be stopped as soon as possible. An event will be fired
     * once its stopped.
     * </p>
     * <p>
     * The algorithms are not required to stop immediately, as this would
     * require too many checks within the algorithm.
     * </p>
     */
    void stop();

}