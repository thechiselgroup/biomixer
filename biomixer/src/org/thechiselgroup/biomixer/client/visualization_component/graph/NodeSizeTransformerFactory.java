package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.NodeSizeTransformer;

public class NodeSizeTransformerFactory {

    public NodeSizeTransformer createConceptNodeSizeTransformer() {
        return new ConceptGraphNodeSizeTransformer();
    }

    public NodeSizeTransformer createOntologyNodeSizeTransformer() {
        return new OntologyGraphNodeSizeTransformer();
    }

}
