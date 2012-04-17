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
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;

// TODO refactor
public class SvgNodeEventHandler implements ChooselEventHandler {

    private RenderedNode renderedNode;

    private GraphDisplayManager graphDisplay;

    private final NodeInteractionManager nodeInteractionManager;

    public SvgNodeEventHandler(RenderedNode renderedNode,
            GraphDisplayManager graphDisplay,
            NodeInteractionManager nodeInteractionManager) {
        this.renderedNode = renderedNode;
        this.nodeInteractionManager = nodeInteractionManager;
        this.graphDisplay = graphDisplay;
    }

    @Override
    public void onEvent(ChooselEvent event) {
        int clientX = event.getClientX();
        int clientY = event.getClientY();

        switch (event.getEventType()) {

        case MOUSE_OVER:
            graphDisplay.onNodeMouseOver(renderedNode, clientX, clientY);
            break;

        case MOUSE_OUT:
            graphDisplay.onNodeMouseOut(renderedNode, clientX, clientY);
            break;

        case MOUSE_UP:
            graphDisplay.onNodeMouseUp();
            break;

        case MOUSE_DOWN:
            graphDisplay.onNodeMouseDown(renderedNode.getNode(), clientX,
                    clientY);
            break;

        default:
            break;

        }
    }

}
