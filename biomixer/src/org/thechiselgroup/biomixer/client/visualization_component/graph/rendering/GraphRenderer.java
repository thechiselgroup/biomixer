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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering;

import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.PopupExpanderSvgComponent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * Handles construction of the actual components displayed for a graph view.
 * 
 * @author drusk
 * 
 */
public interface GraphRenderer {

    // FIXME: generalize
    void addPopup(PopupExpanderSvgComponent popup);

    void bringToForeground(RenderedNode node);

    void clearPopups();

    void removeArc(Arc arc);

    void removeNode(Node node);

    RenderedArc renderArc(Arc arc, RenderedNode source, RenderedNode target);

    RenderedNode renderNode(Node node);

    void setArcStyle(Arc arc, String styleProperty, String styleValue);

    void setBackgroundEventListener(ChooselEventHandler handler);

    void setNodeStyle(Node node, String styleProperty, String styleValue);

    void setViewWideInteractionHandler(ChooselEventHandler handler);

}
