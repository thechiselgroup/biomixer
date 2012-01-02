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

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.UmbrellaException;

/**
 * <p>
 * HandlerManager that supports prioritize calling of
 * {@link PrioritizedEventHandler}s. Regular event handlers are executed with
 * priority <code>NORMAL</code>.
 * </p>
 * <p>
 * <b>IMPLEMENTATION NOTE</b>: Implemented using several internal
 * {@link HandlerManager}s that get called in order. This means that
 * higher-priority event handlers can add lower-priority EventHandlers to a
 * PrioritizedHandlerManager that get executed in the same event processing run.
 * </p>
 * 
 * @author Lars Grammel
 * 
 * @see EventHandlerPriority
 * @see PrioritizedEventHandler
 */
public class PrioritizedHandlerManager {

    private HandlerManager firstPriorityHandlers;

    private HandlerManager normalPriorityHandlers;

    private HandlerManager lastPriorityHandlers;

    public PrioritizedHandlerManager(Object source) {
        assert source != null;

        firstPriorityHandlers = new HandlerManager(source);
        normalPriorityHandlers = new HandlerManager(source);
        lastPriorityHandlers = new HandlerManager(source);
    }

    /**
     * Adds an event handler. If the event handler is a
     * <code>PrioritizedEventHandler</code>, its priority is taken into account.
     * Regular event handlers have priority <code>NORMAL</code>.
     * 
     * @return handler registration that can be used to remove the event
     *         handler.
     */
    public <H extends EventHandler> HandlerRegistration addHandler(
            GwtEvent.Type<H> type, H handler) {

        assert type != null;
        assert handler != null;

        EventHandlerPriority priority = EventHandlerPriority.NORMAL;
        if (handler instanceof PrioritizedEventHandler) {
            priority = ((PrioritizedEventHandler) handler).getPriority();
        }

        switch (priority) {
        case FIRST:
            return firstPriorityHandlers.addHandler(type, handler);
        case NORMAL:
            return normalPriorityHandlers.addHandler(type, handler);
        case LAST:
            return lastPriorityHandlers.addHandler(type, handler);
        }

        throw new IllegalArgumentException("unsupported priority: " + priority);
    }

    private Set<Throwable> doFire(HandlerManager handlers, GwtEvent<?> event,
            Set<Throwable> causes) {

        try {
            handlers.fireEvent(event);
        } catch (Exception e) {
            if (causes == null) {
                causes = new HashSet<Throwable>();
            }

            if (e instanceof UmbrellaException) {
                causes.addAll(((UmbrellaException) e).getCauses());
            } else {
                causes.add(e);
            }
        }

        return causes;
    }

    public void fireEvent(GwtEvent<?> event) {
        assert event != null;

        Set<Throwable> causes = null;

        causes = doFire(firstPriorityHandlers, event, causes);
        causes = doFire(normalPriorityHandlers, event, causes);
        causes = doFire(lastPriorityHandlers, event, causes);

        if (causes != null) {
            throw new UmbrellaException(causes);
        }
    }

    /**
     * Returns the number of handler for a given event type.
     */
    public int getHandlerCount(Type<?> type) {
        return firstPriorityHandlers.getHandlerCount(type)
                + normalPriorityHandlers.getHandlerCount(type)
                + lastPriorityHandlers.getHandlerCount(type);
    }

}