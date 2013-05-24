package org.thechiselgroup.biomixer.client.visualization_component.graph;

import java.util.HashMap;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.NodeSizeTransformer;

public class OntologyGraphNodeSizeTransformer extends NodeSizeTransformer {

    private HashMap<Double, Double> discreteRawSizeToRenderSizeMap = new HashMap<Double, Double>();

    {
        double stepSize = 20.0;
        int i = 1;
        // These scales merely copied from the node size transformer.
        discreteRawSizeToRenderSizeMap.put(0.0, stepSize * i++);
        discreteRawSizeToRenderSizeMap.put(1000.0, stepSize * i++);
        discreteRawSizeToRenderSizeMap.put(5000.0, stepSize * i++);
        discreteRawSizeToRenderSizeMap.put(10000.0, stepSize * i++);
        discreteRawSizeToRenderSizeMap.put(50000.0, stepSize * i++);
        discreteRawSizeToRenderSizeMap.put(100000.0, stepSize * i++);
        discreteRawSizeToRenderSizeMap.put(500000.0, stepSize * i++);
    }

    @Override
    public SizeDouble transform(SizeDouble value) throws Exception {
        // Originally in the AutomaticOntologyExpander, setting for
        // GraphDisplay.NODE_SIZE

        // Assume we are using SquareSizeDouble for now...
        // ontology is set that way, after all.
        double width = scaleForContextRange(value.getWidth());
        // double width = logFunction(value.getHeight());
        // double width = linearFunction(value.getHeight());
        // double width = discretizingFunction(value.getHeight());
        // Assume SquareSizeDouble used...because it is for ontologies.
        return new DefaultSizeDouble(width, width);

    }

    private double linearFunction(double value) {
        return 2 * (4 + Math.sqrt((value) / 10));
    }

    private double logFunction(double value) {
        return 4 + Math.log(value) * 10;
    }

    private double discretizingFunction(double value) {
        double renderSize = 0;
        for (Double lowerCutOff : discreteRawSizeToRenderSizeMap.keySet()) {
            double cutOffRenderSize = discreteRawSizeToRenderSizeMap
                    .get(lowerCutOff);
            // If we're above a given cutoff and it's also the biggest one
            // yet...
            if (lowerCutOff < value && renderSize < cutOffRenderSize) {
                renderSize = cutOffRenderSize;
            }
        }
        return renderSize;
    }
}
