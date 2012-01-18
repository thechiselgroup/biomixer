/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.util.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Enumeration of priorities for event handlers. While it might be better to
 * have numbers for priorities and to perform a sort, this is not possible
 * because several required methods for writing a custom HandlerManager in
 * {@link GwtEvent} are only package visible. Having explicit priorities, on the
 * other, has the advantage that it clarifies the use cases etc., while
 * preventing abuse of the intention of the priority mechanism (e.g. by using
 * priorities to create execution orders).
 * 
 * @author Lars Grammel
 * 
 */
public enum EventHandlerPriority {

    /**
     * Event handlers that should get called first. Examples for this are event
     * handlers that clear cached data.
     */
    FIRST,

    /**
     * Event handlers that should be executed normally. For example, UI updates.
     */
    NORMAL,

    /**
     * Event handlers that can be called last. For example, event handlers that
     * trigger long-running asynchronous operations such as REST service calls.
     */
    LAST

}