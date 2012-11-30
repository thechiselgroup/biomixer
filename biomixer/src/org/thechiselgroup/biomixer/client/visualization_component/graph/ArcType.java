/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;

/**
 * A specific class of arcs. Calculates arc items for a given resource item.
 * 
 * @author Lars Grammel
 * 
 * @see Arc
 * @see VisualItem
 */
public interface ArcType {

    /**
     * Returns all arcs that should be connected to a given {@link VisualItem}
     * in the context of other {@link VisualItem}s.
     * 
     * @param visualItem
     *            {@link VisualItem} for which potential arcs should be returned
     * @param context
     *            context in which the arcs for the {@link VisualItem} should be
     *            calculated.
     * 
     * @return all arcs that are connected to the node representation of the
     *         resource item in the provided context. The arcs do not have to be
     *         the same (in terms of object references) as already returned arcs
     *         for the same resource item. However, their IDs should match: an
     *         equal arc should have an equal id across multiple calls.
     */
    LightweightCollection<Arc> getArcs(VisualItem visualItem,
            VisualItemContainer context);

    /**
     * @return identifier of this arc type. Each ArcType must have a unique
     *         identifier.
     */
    String getArcTypeID();

    /**
     * @return human readable label for the arc type
     */
    String getArcTypeLabel();

    /**
     * @return default color for arcs of this type.
     */
    String getDefaultArcColor();

    /**
     * @return default arc style for arcs of this type.
     */
    String getDefaultArcStyle();

    /**
     * @return default arc head for arcs of this type.
     */
    String getDefaultArcHead();

    /**
     * @return default arc thickness for arcs of this type.
     */
    int getDefaultArcThickness();

    /**
     * Gets thickness for an arc, if the ArcType and Arc have this defined
     * semantically. Individual thickness will be overridden if the
     * overridingThickness argument is greater than 0, or if the ArcType
     * semantics do not allow for individual arc thicknesses.
     * 
     * Set up this way to allow simpler calls; could have left the logic
     * outside, to be used by each caller, but have the override value passed
     * in, pushing the logic into each implementor. Was this silly?
     * 
     * @param arc
     * @param thicknessLevel
     *            thickness to use instead of arc type default. If 0, use
     *            default.
     * @return arc thickness for provided arc, based on this arc type's
     *         semantics.
     */
    int getArcThickness(Arc arc, Integer thicknessLevel);

}