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
package org.thechiselgroup.biomixer.client.core.visualization.model;

import org.thechiselgroup.biomixer.client.core.util.collections.Delta;

import com.google.gwt.event.shared.GwtEvent;

public class VisualItemContainerChangeEvent extends
        GwtEvent<VisualItemContainerChangeEventHandler> {

    public static final GwtEvent.Type<VisualItemContainerChangeEventHandler> TYPE = new GwtEvent.Type<VisualItemContainerChangeEventHandler>();

    private final VisualItemContainer container;

    private final Delta<VisualItem> delta;

    public VisualItemContainerChangeEvent(VisualItemContainer container,
            Delta<VisualItem> delta) {

        assert container != null;
        assert delta != null;

        this.delta = delta;
        this.container = container;
    }

    @Override
    protected void dispatch(VisualItemContainerChangeEventHandler handler) {
        handler.onVisualItemContainerChanged(this);
    }

    @Override
    public GwtEvent.Type<VisualItemContainerChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    public VisualItemContainer getContainer() {
        return container;
    }

    public Delta<VisualItem> getDelta() {
        return delta;
    }

}