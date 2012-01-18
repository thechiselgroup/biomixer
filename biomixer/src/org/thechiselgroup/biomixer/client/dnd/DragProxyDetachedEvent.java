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
package org.thechiselgroup.biomixer.client.dnd;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

/**
 * Fired when the drag proxy is removed (drag and drop operations ends).
 */
public class DragProxyDetachedEvent extends
        GwtEvent<DragProxyDetachedEventHandler> {

    public static final GwtEvent.Type<DragProxyDetachedEventHandler> TYPE = new GwtEvent.Type<DragProxyDetachedEventHandler>();

    private final Widget widget;

    public DragProxyDetachedEvent(Widget widget) {
        assert widget != null;
        this.widget = widget;
    }

    @Override
    protected void dispatch(DragProxyDetachedEventHandler handler) {
        handler.onDragProxyDetached(this);
    }

    @Override
    public GwtEvent.Type<DragProxyDetachedEventHandler> getAssociatedType() {
        return TYPE;
    }

    public Widget getWidget() {
        return widget;
    }

}