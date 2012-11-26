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
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.ArcSettings;

public class MappingArcType implements ArcType {

    public static final String ID = "org.thechiselgroup.biomixer.client.graph.MappingArcType";

    public static final String ARC_LABEL = "mapping of";

    public static final String ARC_COLOR = "#D4D4D4";

    public static final String ARC_STYLE = ArcSettings.ARC_STYLE_DASHED;

    public static final String ARC_HEAD = ArcSettings.ARC_HEAD_TRIANGLE_FULL;

    public static final boolean ARC_DIRECTED = true;

    public static final int ARC_THICKNESS = 1;

    private Arc createArc(String sourceUri, String targetUri) {
        return new Arc(Graph.getArcId(ID, sourceUri, targetUri), sourceUri,
                targetUri, ID, ARC_LABEL, ARC_DIRECTED);
    }

    @Override
    public LightweightCollection<Arc> getArcs(VisualItem visualItem,
            VisualItemContainer context) {

        LightweightList<Arc> arcs = CollectionFactory.createLightweightList();

        String visualItemId = visualItem.getId();
        if (visualItemId.startsWith(Mapping.RESOURCE_URI_PREFIX)) {
            ResourceSet resources = visualItem.getResources();
            assert resources.size() == 1;
            Resource firstResource = resources.getFirstElement();

            String sourceUri = (String) firstResource.getValue(Mapping.SOURCE);
            String targetUri = (String) firstResource.getValue(Mapping.TARGET);

            arcs.add(createArc(sourceUri, visualItemId));
            arcs.add(createArc(visualItemId, targetUri));
        } else if (visualItemId.startsWith(Concept.RESOURCE_URI_PREFIX)) {
            ResourceSet resources = visualItem.getResources();
            assert resources.size() == 1;
            Resource firstResource = resources.getFirstElement();

            for (String mappingUri : firstResource
                    .getUriListValue(Concept.OUTGOING_MAPPINGS)) {
                arcs.add(createArc(visualItemId, mappingUri));
            }

            for (String mappingUri : firstResource
                    .getUriListValue(Concept.INCOMING_MAPPINGS)) {
                arcs.add(createArc(mappingUri, visualItemId));
            }
        }

        return arcs;
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
    public String getDefaultArcHead() {
        return ARC_HEAD;
    }

    @Override
    public int getDefaultArcThickness() {
        return ARC_THICKNESS;
    }

    @Override
    public String getArcTypeLabel() {
        return this.ARC_LABEL;
    }
}