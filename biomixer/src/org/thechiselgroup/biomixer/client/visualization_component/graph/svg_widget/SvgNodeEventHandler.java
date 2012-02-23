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

    private NodeElement nodeElement;

    private GraphSvgDisplay graphDisplay;

    private final NodeInteractionManager nodeInteractionManager;

    public SvgNodeEventHandler(NodeElement nodeElement,
            GraphSvgDisplay graphDisplay,
            NodeInteractionManager nodeInteractionManager) {
        this.nodeElement = nodeElement;
        this.nodeInteractionManager = nodeInteractionManager;
        this.graphDisplay = graphDisplay;
    }

    @Override
    public void onEvent(ChooselEvent event) {
        int clientX = event.getClientX();
        int clientY = event.getClientY();

        switch (event.getEventType()) {

        case MOUSE_OVER:
            graphDisplay.onNodeMouseOver(nodeElement.getId(), clientX, clientY);
            break;

        case MOUSE_OUT:
            graphDisplay.onNodeMouseOut(nodeElement.getId(), clientX, clientY);
            break;

        case MOUSE_UP:
            nodeInteractionManager.onMouseUp();
            break;

        case MOUSE_DOWN:
            nodeInteractionManager.onMouseDown(nodeElement.getId());
            break;

        default:
            break;

        }
    }

}
