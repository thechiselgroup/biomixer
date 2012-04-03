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
 * Combines drag handling and click handling into one handler since SvgElements
 * can currently only have one listener registered.
 * 
 * XXX duplicates code from DragEventHandler
 * 
 * @see DragEventHandler
 * 
 * @author drusk
 * 
 */
public abstract class DragAndClickHandler implements ChooselEventHandler {

    private int lastMouseX = 0;

    private int lastMouseY = 0;

    private boolean mouseDown = false;

    public abstract void handleClick(ClickEvent clickEvent);

    public abstract void handleDrag(DragEvent dragEvent);

    @Override
    public void onEvent(ChooselEvent event) {
        int clientX = event.getClientX();
        int clientY = event.getClientY();

        switch (event.getEventType()) {
        case MOUSE_DOWN:
            mouseDown = true;
            break;

        case MOUSE_MOVE:
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

        case CLICK:
            handleClick(new ClickEvent(clientX, clientY, this));
            break;

        default:
            break;
        }

    }

}
