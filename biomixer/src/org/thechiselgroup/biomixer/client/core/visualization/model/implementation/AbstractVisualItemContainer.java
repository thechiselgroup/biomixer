/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.visualization.model.implementation;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.event.PrioritizedHandlerManager;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEventHandler;

import com.google.gwt.event.shared.HandlerRegistration;

public abstract class AbstractVisualItemContainer implements
        VisualItemContainer {

    private transient PrioritizedHandlerManager handlerManager;

    private ErrorHandler errorHandler;

    public AbstractVisualItemContainer(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.handlerManager = new PrioritizedHandlerManager(this);
    }

    @Override
    public HandlerRegistration addHandler(
            VisualItemContainerChangeEventHandler handler) {

        assert handler != null;
        return handlerManager.addHandler(VisualItemContainerChangeEvent.TYPE,
                handler);
    }

    public void fireVisualItemContainerChangeEvent(Delta<VisualItem> delta) {
        assert delta != null;

        if (delta.isEmpty()) {
            return;
        }

        try {
            handlerManager.fireEvent(new VisualItemContainerChangeEvent(this,
                    delta));
        } catch (Throwable ex) {
            errorHandler.handleError(ex);
        }
    }
}
