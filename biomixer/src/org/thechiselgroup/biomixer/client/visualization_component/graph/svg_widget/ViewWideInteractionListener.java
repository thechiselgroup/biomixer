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
 * Detects events on the entire view.
 * 
 * @author drusk
 * 
 */
public class ViewWideInteractionListener implements ChooselEventHandler {

    private final NodeInteractionManager nodeInteractionManager;

    private ExpanderPopupManager expanderPopupManager;

    public ViewWideInteractionListener(
            NodeInteractionManager nodeInteractionManager,
            ExpanderPopupManager expanderPopupManager) {
        this.nodeInteractionManager = nodeInteractionManager;
        this.expanderPopupManager = expanderPopupManager;
    }

    @Override
    public void onEvent(ChooselEvent event) {

        switch (event.getEventType()) {

        case MOUSE_DOWN:
            // remove any popup tab
            expanderPopupManager.onMouseDown();
            break;

        case MOUSE_MOVE:
            // inform NodeInteractionManager of movement
            nodeInteractionManager.onMouseMove(event.getClientX(),
                    event.getClientY());
            break;

        default:
            break;

        }

    }

}
