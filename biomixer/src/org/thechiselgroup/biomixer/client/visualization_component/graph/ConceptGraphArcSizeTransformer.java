package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.ArcSizeTransformer;

public class ConceptGraphArcSizeTransformer extends ArcSizeTransformer {

    @Override
    public Double transform(Double value) throws Exception {
        // return logFunction(value);
        // return linearFunction(value);
        // return discretizingFunction(value);
        // For concepts, there is usally nothing to modulate thickness.
        return value;
    }

}
