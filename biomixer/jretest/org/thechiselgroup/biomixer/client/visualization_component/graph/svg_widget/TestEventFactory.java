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

public class TestEventFactory {

    public static ChooselEvent createMouseclickEvent() {
        return new ChooselEvent(Type.CLICK);
    }

    public static ChooselEvent createMouseDownEvent(int x, int y) {
        return new ChooselEvent(Type.MOUSE_DOWN, x, y);
    }

    public static ChooselEvent createMouseMoveEvent(int x, int y) {
        return new ChooselEvent(Type.MOUSE_MOVE, x, y);
    }

    public static ChooselEvent createMouseOutEvent() {
        return new ChooselEvent(Type.MOUSE_OUT);
    }

    public static ChooselEvent createMouseOverEvent() {
        return new ChooselEvent(Type.MOUSE_OVER);
    }

}
