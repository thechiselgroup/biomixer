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
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;

/**
 * Can be used to handle various drag events where you need to know the delta x
 * and delta y resulting from the drag.
 * 
 * @author drusk
 * 
 */
public abstract class DragEventHandler implements ChooselEventHandler {

    private int lastMouseX = 0;

    private int lastMouseY = 0;

    private boolean mouseDown = false;

    public abstract void handleDrag(DragEvent dragEvent);

    @Override
    public void onEvent(ChooselEvent event) {

        switch (event.getEventType()) {
        case MOUSE_DOWN:
            mouseDown = true;
            break;

        case MOUSE_MOVE:
            int clientX = event.getClientX();
            int clientY = event.getClientY();
            if (mouseDown) {
                handleDrag(new DragEvent(clientX - lastMouseX, clientY
                        - lastMouseY, this));
            }
            lastMouseX = clientX;
            lastMouseY = clientY;
            break;

        case MOUSE_UP:
            mouseDown = false;
            break;

        default:
            break;
        }

    }

}
