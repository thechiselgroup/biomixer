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
package org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget;

import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent.Type;

import com.google.gwt.user.client.Event;

public class TestEventFactory {

    public static ChooselEvent createMouseclickEvent() {
        return new ChooselEvent(((Event) null), Type.CLICK,
                ChooselEvent.UNKNOWN, ChooselEvent.UNKNOWN);
    }

    public static ChooselEvent createMouseDownEvent(int x, int y) {
        return new ChooselEvent(((Event) null), Type.MOUSE_DOWN, x, y);
    }

    public static ChooselEvent createMouseMoveEvent(int x, int y) {
        return new ChooselEvent(((Event) null), Type.MOUSE_MOVE, x, y);
    }

    public static ChooselEvent createMouseOutEvent() {
        return new ChooselEvent(((Event) null), Type.MOUSE_OUT,
                ChooselEvent.UNKNOWN, ChooselEvent.UNKNOWN);
    }

    public static ChooselEvent createMouseOverEvent() {
        return new ChooselEvent(((Event) null), Type.MOUSE_OVER,
                ChooselEvent.UNKNOWN, ChooselEvent.UNKNOWN);
    }

}
