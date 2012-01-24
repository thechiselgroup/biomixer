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
package org.thechiselgroup.biomixer.client.dnd;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

/**
 * Part of solution to problem that MouseOutEvents do not get fired if mouse out
 * occurs because of DOM changes (instead of mouse movement). Instead, there are
 * {@link DragProxyAttachedEvent} and {@link DragProxyDetachedEvent} that can be
 * handled, even for parent widgets.
 */
public final class DragProxyUtils {

    /**
     * <p>
     * Fires a {@link DragProxyAttachedEvent} on the widget and its parents.
     * </p>
     * <p>
     * Workaround for the problem that mouseout/over events do not get triggered
     * if a HTML element is created below the cursor. Events would be hard to
     * implement in this case, because the parent might not know about the child
     * (e.g. window might not know about some widget created inside presenters).
     * </p>
     */
    public static void fireDragProxyAttached(Widget w) {
        fireEventOnWidgetAndParents(w, new DragProxyAttachedEvent(w));
    }

    /**
     * <p>
     * Fires a {@link DragProxyDetachedEvent} on the widget and its parents.
     * </p>
     * <p>
     * Workaround for the problem that mouseout/over events do not get triggered
     * if a HTML element is created below the cursor. Events would be hard to
     * implement in this case, because the parent might not know about the child
     * (e.g. window might not know about some widget created inside presenters).
     * </p>
     */
    public static void fireDragProxyDetached(Widget w) {
        fireEventOnWidgetAndParents(w, new DragProxyDetachedEvent(w));
    }

    private static void fireEventOnWidgetAndParents(Widget w, GwtEvent<?> event) {
        while (w != null) {
            w.fireEvent(event);
            w = w.getParent();
        }
    }

    private DragProxyUtils() {
    }

}