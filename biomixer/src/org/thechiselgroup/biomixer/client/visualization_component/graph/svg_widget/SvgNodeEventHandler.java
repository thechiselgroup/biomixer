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

public class SvgNodeEventHandler implements ChooselEventHandler {

    private String nodeId;

    private GraphSvgDisplay graphDisplay;

    private boolean mouseDown = false;

    private int lastMouseX = -1;

    private int lastMouseY = -1;

    private boolean movedSinceMouseDown = false;

    public SvgNodeEventHandler(String nodeId, GraphSvgDisplay graphDisplay) {
        this.nodeId = nodeId;
        this.graphDisplay = graphDisplay;
    }

    @Override
    public void onEvent(ChooselEvent event) {
        int clientX = event.getClientX();
        int clientY = event.getClientY();

        switch (event.getEventType()) {

        case MOUSE_OVER:
            graphDisplay.onNodeMouseOver(nodeId, clientX, clientY);
            break;

        case MOUSE_OUT:
            graphDisplay.onNodeMouseOut(nodeId, clientX, clientY);
            break;

        case MOUSE_UP:
            mouseDown = false;
            if (!movedSinceMouseDown) {
                graphDisplay.onNodeMouseClick(nodeId, clientX, clientY);
            }
            movedSinceMouseDown = false;
            break;

        case MOUSE_DOWN:
            mouseDown = true;
            lastMouseX = event.getClientX();
            lastMouseY = event.getClientY();
            break;

        case MOUSE_MOVE:
            if (mouseDown) {
                assert lastMouseX >= 0;
                assert lastMouseY >= 0;
                int deltaX = clientX - lastMouseX;
                int deltaY = clientY - lastMouseY;
                lastMouseX = clientX;
                lastMouseY = clientY;
                movedSinceMouseDown = true;
                graphDisplay.onNodeDrag(nodeId, deltaX, deltaY);
            }
            break;

        default:
            break;

        }
    }

}
