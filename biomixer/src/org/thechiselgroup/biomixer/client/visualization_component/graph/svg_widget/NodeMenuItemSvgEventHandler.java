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

import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMenuItemClickedHandler;

public class NodeMenuItemSvgEventHandler implements ChooselEventHandler {

    private final Node node;

    private final BoxedTextSvgElement expanderEntry;

    private final NodeMenuItemClickedHandler clickHandler;

    public NodeMenuItemSvgEventHandler(Node node,
            BoxedTextSvgElement expanderEntry,
            NodeMenuItemClickedHandler clickHandler) {
        this.node = node;
        this.expanderEntry = expanderEntry;
        this.clickHandler = clickHandler;
    }

    @Override
    public void onEvent(ChooselEvent event) {

        switch (event.getEventType()) {

        case MOUSE_OVER:
            // give rect highlight color
            expanderEntry.setBackgroundColor(Colors.BLUE_1);
            break;

        case MOUSE_OUT:
            // give rect default color
            expanderEntry.setBackgroundColor(Colors.WHITE);
            break;

        case CLICK:
            // use handler
            clickHandler.onNodeMenuItemClicked(node);
            break;

        default:
            break;

        }

    }

}
