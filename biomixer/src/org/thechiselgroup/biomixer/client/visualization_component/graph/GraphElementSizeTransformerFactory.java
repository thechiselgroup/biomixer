package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.ArcSizeTransformer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.NodeSizeTransformer;

public class GraphElementSizeTransformerFactory {

    public NodeSizeTransformer createConceptNodeSizeTransformer() {
        return new ConceptGraphNodeSizeTransformer();
    }

    public NodeSizeTransformer createOntologyNodeSizeTransformer() {
        return new OntologyGraphNodeSizeTransformer();
    }

    public ArcSizeTransformer createConceptArcSizeTransformer() {
        return new ConceptGraphArcSizeTransformer();
    }

    public ArcSizeTransformer createOntologyMappingArcSizeTransformer() {
        return new OntologyGraphMappingArcSizeTransformer();
    }

}
