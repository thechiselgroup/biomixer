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

import java.util.Set;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

import com.google.gwt.user.client.ui.Widget;

/**
 * Handles construction of the actual components displayed for a graph view.
 * 
 * @author drusk
 * 
 */
public interface GraphRenderer {

    void bringToForeground(RenderedNode node);

    void checkIfScrollbarsNeeded();

    SizeDouble getGraphSize();

    Widget getGraphWidget();

    RenderedArc getRenderedArc(Arc arc);

    RenderedNode getRenderedNode(Node node);

    boolean isWidgetInitialized();

    void removeAllNodeExpanders();

    void removeArc(Arc arc);

    void removeNode(Node node);

    void removeNodeExpander(RenderedNodeExpander expander);

    RenderedArc renderArc(Arc arc, RenderedNode source, RenderedNode target);

    RenderedNode renderNode(Node node);

    RenderedNodeExpander renderNodeExpander(PointDouble topLeftLocation,
            Set<String> expanderLabels);

    void setArcStyle(Arc arc, String styleProperty, String styleValue);

    void setBackgroundEventListener(ChooselEventHandler handler);

    void setGraphHeight(int height);

    void setGraphWidth(int width);

    void setNodeStyle(Node node, String styleProperty, String styleValue);

    void setViewWideInteractionHandler(ChooselEventHandler handler);

}
