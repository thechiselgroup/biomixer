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

import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.Ontology;
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

    public static final String ARC_COLOR = "#D4D4D4";

    public static final String ARC_STYLE = ArcSettings.ARC_STYLE_DASHED;

    public static final boolean ARC_DIRECTED = false;

    // TODO This isn't right! I need to have the thickness vary by arc, but all
    // arcs use the same thickness it appears...
    public static final int ARC_THICKNESS = 1;

    private final ResourceAccessor resourceAccessor;

    public OntologyMappingArcType(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    private Arc createArc(String concept1Uri, String concept2Uri) {
        boolean isConcept1First = concept1Uri.compareTo(concept2Uri) < 0;
        String firstUri = isConcept1First ? concept1Uri : concept2Uri;
        String secondUri = isConcept1First ? concept2Uri : concept1Uri;

        return new Arc(Graph.getArcId(ID, firstUri, secondUri), firstUri,
                secondUri, ID, ARC_DIRECTED);
    }

    // Adapted from both undirected DirectConceptMapping and MappingArcType
    // I don't yet understand arc usage enough to say if it is correct.
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

            for (String uri : resource
                    .getUriListValue(Ontology.OUTGOING_MAPPINGS)) {
                if (resourceAccessor.contains(uri)) {
                    Resource mapping = resourceAccessor.getByUri(uri);
                    String targetResource = (String) mapping
                            .getValue(Mapping.TARGET);
                    arcItems.add(createArc(visualItem.getId(), targetResource));
                }
            }
            for (String uri : resource
                    .getUriListValue(Ontology.INCOMING_MAPPINGS)) {
                if (resourceAccessor.contains(uri)) {
                    Resource mapping = resourceAccessor.getByUri(uri);
                    String sourceResource = (String) mapping
                            .getValue(Mapping.SOURCE);
                    arcItems.add(createArc(sourceResource, visualItem.getId()));
                }
            }
        }

        return arcItems;
    }

    // // From undirected DirectConceptMapping
    // @Override
    // public LightweightCollection<Arc> getArcs(VisualItem visualItem,
    // VisualItemContainer context) {
    //
    // LightweightList<Arc> arcItems = CollectionFactory
    // .createLightweightList();
    //
    // // TODO clean up filter code
    // if (visualItem.getId().startsWith(Concept.RESOURCE_URI_PREFIX)) {
    // ResourceSet resources = visualItem.getResources();
    // assert resources.size() == 1;
    // Resource resource = resources.getFirstElement();
    //
    // for (String uri : resource
    // .getUriListValue(Concept.OUTGOING_MAPPINGS)) {
    // if (resourceAccessor.contains(uri)) {
    // Resource mapping = resourceAccessor.getByUri(uri);
    // String targetResource = (String) mapping
    // .getValue(Mapping.TARGET);
    // arcItems.add(createArc(visualItem.getId(), targetResource));
    // }
    // }
    // for (String uri : resource
    // .getUriListValue(Concept.INCOMING_MAPPINGS)) {
    // if (resourceAccessor.contains(uri)) {
    // Resource mapping = resourceAccessor.getByUri(uri);
    // String sourceResource = (String) mapping
    // .getValue(Mapping.SOURCE);
    // arcItems.add(createArc(sourceResource, visualItem.getId()));
    // }
    // }
    // }
    //
    // return arcItems;
    // }
    //
    // // From MappingArcType
    // public LightweightCollection<Arc> getArcs1(VisualItem visualItem,
    // VisualItemContainer context) {
    //
    // LightweightList<Arc> arcs = CollectionFactory.createLightweightList();
    //
    // String visualItemId = visualItem.getId();
    // if (visualItemId.startsWith(Mapping.RESOURCE_URI_PREFIX)) {
    // ResourceSet resources = visualItem.getResources();
    // assert resources.size() == 1;
    // Resource firstResource = resources.getFirstElement();
    //
    // String sourceUri = (String) firstResource.getValue(Mapping.SOURCE);
    // String targetUri = (String) firstResource.getValue(Mapping.TARGET);
    //
    // arcs.add(createArc(sourceUri, visualItemId));
    // arcs.add(createArc(visualItemId, targetUri));
    // } else if (visualItemId.startsWith(Concept.RESOURCE_URI_PREFIX)) {
    // ResourceSet resources = visualItem.getResources();
    // assert resources.size() == 1;
    // Resource firstResource = resources.getFirstElement();
    //
    // for (String mappingUri : firstResource
    // .getUriListValue(Concept.OUTGOING_MAPPINGS)) {
    // arcs.add(createArc(visualItemId, mappingUri));
    // }
    //
    // for (String mappingUri : firstResource
    // .getUriListValue(Concept.INCOMING_MAPPINGS)) {
    // arcs.add(createArc(mappingUri, visualItemId));
    // }
    // }
    //
    // return arcs;
    // }

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