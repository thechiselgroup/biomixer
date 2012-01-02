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
package org.thechiselgroup.biomixer.client.graph;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.ResourceSet;
import org.thechiselgroup.choosel.core.client.resources.ResourceSetUtils;
import org.thechiselgroup.choosel.core.client.util.collections.CollectionFactory;
import org.thechiselgroup.choosel.core.client.util.collections.LightweightCollection;
import org.thechiselgroup.choosel.core.client.util.collections.LightweightList;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItemContainer;
import org.thechiselgroup.choosel.visualization_component.graph.client.ArcType;
import org.thechiselgroup.choosel.visualization_component.graph.client.Graph;
import org.thechiselgroup.choosel.visualization_component.graph.client.widget.Arc;
import org.thechiselgroup.choosel.visualization_component.graph.client.widget.ArcSettings;

public class ConceptArcType implements ArcType {

    public static final String ID = "org.thechiselgroup.biomixer.client.graph.ConceptArcType";

    public static final String ARC_COLOR = "#AFC6E5";

    public static final String ARC_STYLE = ArcSettings.ARC_STYLE_SOLID;

    public static final boolean ARC_DIRECTED = true;

    public static final int ARC_THICKNESS = 1;

    private Arc createArc(String sourceUri, String targetUri) {
        return new Arc(Graph.getArcId(ID, sourceUri, targetUri), sourceUri,
                targetUri, ID, ARC_DIRECTED);
    }

    @Override
    public LightweightCollection<Arc> getArcs(VisualItem visualItem,
            VisualItemContainer context) {

        LightweightList<Arc> arcItems = CollectionFactory
                .createLightweightList();

        // TODO clean up filter code
        String visualItemId = visualItem.getId();
        if (visualItemId.startsWith(Concept.RESOURCE_URI_PREFIX)) {
            ResourceSet resources = visualItem.getResources();
            assert resources.size() == 1;
            Resource firstResource = resources.getFirstElement();

            for (String parentUri : firstResource
                    .getUriListValue(Concept.PARENT_CONCEPTS)) {
                arcItems.add(createArc(visualItemId, parentUri));
            }
            for (String childUri : firstResource
                    .getUriListValue(Concept.CHILD_CONCEPTS)) {
                arcItems.add(createArc(childUri, visualItemId));
            }
        }

        return arcItems;
    }

    @Override
    public String getArcTypeID() {
        return ID;
    }

    @Override
    public String getDefaultArcColor() {
        return ARC_COLOR;
    }

    @Override
    public String getDefaultArcStyle() {
        return ARC_STYLE;
    }

    @Override
    public int getDefaultArcThickness() {
        return ARC_THICKNESS;
    }
}