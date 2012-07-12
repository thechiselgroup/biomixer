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

import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * A displayable node.
 * 
 * @author drusk
 * 
 */
public interface RenderedNode {

    /**
     * 
     * @param arc
     *            an arc which has this node as either a source or target
     */
    void addConnectedArc(RenderedArc arc);

    PointDouble getCentre();

    /**
     * 
     * @return the arcs with this node as either a source or target
     */
    List<RenderedArc> getConnectedArcs();

    /**
     * 
     * @return the location where a node's expander should be placed when its
     *         display is triggered.
     */
    PointDouble getExpanderPopupLocation();

    double getLeftX();

    Node getNode();

    SizeDouble getSize();

    PointDouble getTopLeft();

    double getTopY();

    String getType();

    void removeConnectedArc(RenderedArc arc);

    void setBackgroundColor(String color);

    /**
     * 
     * @param handler
     *            the event handler for most interactions with a node, such as
     *            click, drag, mouse over, etc.
     */
    void setBodyEventHandler(ChooselEventHandler handler);

    void setBorderColor(String color);

    /**
     * 
     * @param handler
     *            the event handler for creating/displaying node expansion
     *            options.
     */
    void setExpansionEventHandler(ChooselEventHandler handler);

    void setFontColor(String color);

    void setFontWeight(String fontWeight);

    void setPosition(double x, double y);

}
