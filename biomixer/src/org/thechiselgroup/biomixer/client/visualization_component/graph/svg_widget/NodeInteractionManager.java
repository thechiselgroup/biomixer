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

/**
 * Keeps track of the node that is currently "mouse down", and manages its
 * movements.
 * 
 * @author drusk
 * 
 */
public class NodeInteractionManager {

    /*
     * Keeps track of the currently mouse down node. Null if no node is mouse
     * down.
     */
    private String mouseDownNodeId = null;

    private int lastMouseX = 0;

    private int lastMouseY = 0;

    private boolean movedSinceMouseDown = false;

    private final GraphDisplayController graphDisplay;

    public NodeInteractionManager(GraphDisplayController graphDisplay) {
        this.graphDisplay = graphDisplay;
    }

    public void onMouseDown(String nodeId, ChooselEvent event, int currentX,
            int currentY) {
        mouseDownNodeId = nodeId;
        movedSinceMouseDown = false;
        lastMouseX = currentX;
        lastMouseY = currentY;
    }

    public void onMouseMove(ChooselEvent event, int currentX, int currentY) {
        // Was trying to see if mouse button is still down,
        // but browsers apparently do not support mouse polling.
        // I wanted this to prevent the mouse-stuck-on-node bug,
        // but I cannot do it this way.

        if (mouseDownNodeId != null) {
            int deltaX = currentX - lastMouseX;
            int deltaY = currentY - lastMouseY;
            graphDisplay.onNodeDrag(event, mouseDownNodeId, deltaX, deltaY);
            movedSinceMouseDown = true;
        }
        lastMouseX = currentX;
        lastMouseY = currentY;
    }

    public void onMouseUp(ChooselEvent event) {
        if (!movedSinceMouseDown && mouseDownNodeId != null) {
            graphDisplay.onNodeMouseClick(mouseDownNodeId, event, lastMouseX,
                    lastMouseY);
        }
        mouseDownNodeId = null;
    }

}
