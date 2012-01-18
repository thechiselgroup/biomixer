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
package org.thechiselgroup.biomixer.client.core.label;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class DefaultHasLabel implements HasLabel {

    private HandlerManager eventBus;

    private HasLabel eventSource;

    private String label = null;

    public DefaultHasLabel(HasLabel eventSource) {
        assert eventSource != null;

        this.eventSource = eventSource;
        this.eventBus = new HandlerManager(eventSource);
    }

    @Override
    public HandlerRegistration addLabelChangedEventHandler(
            LabelChangedEventHandler eventHandler) {

        assert eventHandler != null;

        return eventBus.addHandler(LabelChangedEvent.TYPE, eventHandler);
    }

    @Override
    public String getLabel() {
        return label == null ? "" : label;
    }

    @Override
    public boolean hasLabel() {
        return label != null;
    }

    @Override
    public void setLabel(String newLabel) {
        if (newLabel == this.label
                || (newLabel != null && newLabel.equals(this.label))) {
            return;
        }

        String oldLabel = this.label;

        this.label = newLabel;

        eventBus.fireEvent(new LabelChangedEvent(eventSource, newLabel,
                oldLabel));
    }

}