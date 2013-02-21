package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.NodeSizeTransformer;

public class OntologyGraphNodeSizeTransformer extends NodeSizeTransformer {

    @Override
    public SizeDouble transform(SizeDouble value) throws Exception {
        // Originally in the AutomaticOntologyExpander, setting for
        // GraphDisplay.NODE_SIZE

        // Assume we are using SquareSizeDouble for now...
        // ontology is set that way, after all.
        double width = scaleForContextRange(value.getWidth());
        // double width = logFunction(value.getHeight());
        // double width = linearFunction(value.getHeight());
        return new DefaultSizeDouble(width, width);

    }

    private double linearFunction(double value) {
        return 2 * (4 + Math.sqrt((value) / 10));
    }

    private double logFunction(double value) {
        return 4 + Math.log(value) * 10;
    }

}
