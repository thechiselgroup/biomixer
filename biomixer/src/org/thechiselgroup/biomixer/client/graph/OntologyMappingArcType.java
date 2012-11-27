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

import org.thechiselgroup.biomixer.client.Ontology;
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

/**
 * This arc type represents an aggregation of concept mappings, for use when
 * rendering ontologies as nodes. Thus this arc represents the amount of
 * relatedness in terms of mappings between two ontologies. Thickness is
 * normally modulated by the number of mappings between a pair of ontologies.
 * 
 * @author everbeek
 * 
 */
public class OntologyMappingArcType implements ArcType {

    public static final String ID = "org.thechiselgroup.biomixer.client.graph.OntologyMappingArcType";

    public static final String ARC_LABEL = "maps to";

    public static final String ARC_COLOR = "#D4D4D4";

    public static final String ARC_STYLE = ArcSettings.ARC_STYLE_DASHED;

    public static final boolean ARC_DIRECTED = false;

    public static final int ARC_THICKNESS = 1;

    public static final String ARC_HEAD = ArcSettings.ARC_HEAD_NONE;

    private Arc createArc(String ontology1Uri, String ontology2Uri,
            int numberOfMappings) {
        boolean isOntology1First = ontology1Uri.compareTo(ontology2Uri) < 0;
        String firstUri = isOntology1First ? ontology1Uri : ontology2Uri;
        String secondUri = isOntology1First ? ontology2Uri : ontology1Uri;

        Arc arc = new Arc(Graph.getArcId(ID, firstUri, secondUri), firstUri,
                secondUri, ID, getArcTypeLabel(), ARC_DIRECTED);
        int scaledThickness = (int) Math.round((1 + Math
                .sqrt((numberOfMappings) / 1)));
        arc.setSize(scaledThickness);
        return arc;
    }

    // Adapted mostly from MappingArcType, but also from DirectConceptMapping.
    @Override
    public LightweightCollection<Arc> getArcs(VisualItem visualItem,
            VisualItemContainer context) {

        LightweightList<Arc> arcItems = CollectionFactory
                .createLightweightList();

        // TODO clean up filter code
        if (visualItem.getId().startsWith(Ontology.RESOURCE_URI_PREFIX)) {
            ResourceSet resources = visualItem.getResources();
            assert resources.size() == 1;
            Resource resource = resources.getFirstElement();
            String visualItemId = visualItem.getId();

            // From Mapping version
            for (String targetUri : resource
                    .getUriListValue(Ontology.OUTGOING_MAPPINGS)) {
                int numberOfMappings = Ontology.getOntologyCount(targetUri);
                String pureTargetUri = Ontology.getPureOntologyURI(targetUri);
                arcItems.add(createArc(visualItemId, pureTargetUri,
                        numberOfMappings));
            }

            for (String sourceUri : resource
                    .getUriListValue(Ontology.INCOMING_MAPPINGS)) {
                int numberOfMappings = Ontology.getOntologyCount(sourceUri);
                String pureSourceUri = Ontology.getPureOntologyURI(sourceUri);
                arcItems.add(createArc(pureSourceUri, visualItemId,
                        numberOfMappings));
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

    @Override
    public int getArcThickness(Arc arc, Integer thicknessLevel) {
        // Return number of concept mappings that are aggregated over
        return (0 == thicknessLevel) ? arc.getSize() : thicknessLevel;
    }

    @Override
    public String getDefaultArcHead() {
        return ARC_HEAD;
    }

    @Override
    public String getArcTypeLabel() {
        return ARC_LABEL;
    }
}