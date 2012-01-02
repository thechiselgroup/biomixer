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

import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.choosel.core.client.util.collections.LightweightCollection;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItemContainer;

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
     * Returns all arcs that should be connected to a given {@link VisualItem} in
     * the context of other {@link VisualItem}s.
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
     * @return default color for arcs of this type.
     */
    String getDefaultArcColor();

    /**
     * @return default arc style for arcs of this type.
     */
    String getDefaultArcStyle();

    /**
     * @return default arc thickness for arcs of this type.
     */
    int getDefaultArcThickness();

}