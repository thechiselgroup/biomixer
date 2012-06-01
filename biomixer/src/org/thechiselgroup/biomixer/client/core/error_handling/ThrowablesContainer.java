/*******************************************************************************
 * Copyright 2012 David Rusk 
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
package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.ArrayList;
import java.util.List;

public class ThrowablesContainer {

    private final List<ThrowableCaught> throwables = new ArrayList<ThrowableCaught>();

    private final List<ThrowablesContainerEventListener> eventListeners = new ArrayList<ThrowablesContainerEventListener>();

    public void addListener(ThrowablesContainerEventListener listener) {
        eventListeners.add(listener);
    }

    public void addThrowableCaught(ThrowableCaught throwableCaught) {
        throwables.add(throwableCaught);
        fireThrowableAddedEvent(new ThrowableCaughtEvent(throwableCaught, this));
    }

    private void fireThrowableAddedEvent(ThrowableCaughtEvent event) {
        for (ThrowablesContainerEventListener listener : eventListeners) {
            // in case something goes wrong with updating the UI when errors
            // have been added to this list, we want to continue.
            try {
                listener.onThrowableCaughtAdded(event);
            } catch (Exception e) {
                // XXX potentially log the error.
            }
        }
    }

    private void fireThrowableRemovedEvent(ThrowableCaughtEvent event) {
        for (ThrowablesContainerEventListener listener : eventListeners) {
            listener.onThrowableCaughtRemoved(event);
        }
    }

    public List<ThrowableCaught> getThrowablesCaught() {
        return throwables;
    }

    public void removeListener(ThrowablesContainerEventListener listener) {
        eventListeners.remove(listener);
    }

    public void removeThrowableCaught(ThrowableCaught throwableCaught) {
        assert throwables.contains(throwableCaught);

        throwables.remove(throwableCaught);
        fireThrowableRemovedEvent(new ThrowableCaughtEvent(throwableCaught,
                this));
    }
}
