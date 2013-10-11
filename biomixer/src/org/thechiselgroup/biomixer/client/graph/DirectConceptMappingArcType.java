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
import org.thechiselgroup.biomixer.client.core.resources.ResourceAccessor;
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

/**
 * Undirected arcs between concepts that are are mapped.
 * 
 * <p>
 * <b>IMPLEMENTATION NOTE</b>: The the order of source and target (and thus also
 * the id) depends on the lexicographical order of the URIs of the concepts.
 * That way, both concepts provide the same arc for the same mapping.
 * </p>
 * 
 * @author Lars Grammel
 */
public class DirectConceptMappingArcType implements ArcType {

    public static final String ID = "org.thechiselgroup.biomixer.client.graph.DirectConceptMappingArcType";

    public static final String ARC_LABEL = "maps to";

    public static final String ARC_COLOR = "#D4D4D4";

    public static final String ARC_STYLE = ArcSettings.ARC_STYLE_DASHED;

    public static final String ARC_HEAD = ArcSettings.ARC_HEAD_NONE;

    public static final boolean ARC_DIRECTED = false;

    public static final int ARC_THICKNESS = 3;

    private final ResourceAccessor resourceAccessor;

    public DirectConceptMappingArcType(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    private Arc createArc(String concept1Uri, String concept2Uri) {
        // Stopped doing this in ontology arcs. Had to put this back to prevent
        // duplicate arcs. They were visible as dashed lines that turned into
        // solid as you dragged the nodes. Directed/undirected is sort of
        // a pain given the current classes...
        boolean isConcept1First = concept1Uri.compareTo(concept2Uri) < 0;
        String firstUri = isConcept1First ? concept1Uri : concept2Uri;
        String secondUri = isConcept1First ? concept2Uri : concept1Uri;

        // String firstUri = concept1Uri;
        // String secondUri = concept2Uri;

        return new Arc(Graph.getArcId(ID, firstUri, secondUri), firstUri,
                secondUri, ID, ARC_LABEL, ARC_DIRECTED);
    }

    @Override
    public LightweightCollection<Arc> getArcs(VisualItem visualItem,
            VisualItemContainer context) {

        LightweightList<Arc> arcItems = CollectionFactory
                .createLightweightList();

        // TODO clean up filter code
        if (visualItem.getId().startsWith(Concept.RESOURCE_URI_PREFIX)) {
            ResourceSet resources = visualItem.getResources();
            assert resources.size() == 1;
            Resource resource = resources.getFirstElement();

            for (String uri : resource
                    .getUriListValue(Concept.OUTGOING_MAPPINGS)) {
                if (resourceAccessor.contains(uri)) {
                    Resource mapping = resourceAccessor.getByUri(uri);
                    String targetResource = Mapping.getTargetUri(mapping);
                    arcItems.add(createArc(visualItem.getId(), targetResource));
                }
            }
            for (String uri : resource
                    .getUriListValue(Concept.INCOMING_MAPPINGS)) {
                if (resourceAccessor.contains(uri)) {
                    Resource mapping = resourceAccessor.getByUri(uri);
                    String sourceResource = Mapping.getSourceUri(mapping);;
                    arcItems.add(createArc(sourceResource, visualItem.getId()));
                }
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
    public String getDefaultArcHead() {
        return ARC_HEAD;
    }

    @Override
    public int getDefaultArcThickness() {
        return ARC_THICKNESS;
    }

    @Override
    public int getArcThickness(Arc arc, Integer thicknessLevel) {
        return (0 == thicknessLevel) ? this.getDefaultArcThickness()
                : thicknessLevel;
    }

    @Override
    public String getArcTypeLabel() {
        return ARC_LABEL;

    }
}