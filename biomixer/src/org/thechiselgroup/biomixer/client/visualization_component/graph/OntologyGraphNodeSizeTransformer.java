package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.NodeSizeTransformer;

public class OntologyGraphNodeSizeTransformer extends NodeSizeTransformer {

    @Override
    public SizeDouble transform(SizeDouble value) throws Exception {
        // Originally in the AutomaticOntologyExpander, setting for
        // GraphDisplay.NODE_SIZE
        double width = 2 * (4 + Math.sqrt((value.getWidth()) / 10));
        double height = 2 * (4 + Math.sqrt((value.getHeight()) / 10));
        return new DefaultSizeDouble(height, width);
    }
}
