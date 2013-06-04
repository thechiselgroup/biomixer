package org.thechiselgroup.biomixer.client.visualization_component.graph;

import java.util.HashMap;

import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.ArcSizeTransformer;

public class OntologyGraphMappingArcSizeTransformer extends ArcSizeTransformer {

    private HashMap<Double, Double> discreteRawSizeToRenderSizeMap = new HashMap<Double, Double>();

    {
        discreteRawSizeToRenderSizeMap.put(0.0, 1.0);
        discreteRawSizeToRenderSizeMap.put(200.0, 2.0);
        discreteRawSizeToRenderSizeMap.put(400.0, 3.0);
        discreteRawSizeToRenderSizeMap.put(2000.0, 4.0);
        discreteRawSizeToRenderSizeMap.put(4000.0, 5.0);
        discreteRawSizeToRenderSizeMap.put(20000.0, 7.0);
        discreteRawSizeToRenderSizeMap.put(40000.0, 10.0);
    }

    @Override
    public Double transform(Double value) throws Exception {
        // return logFunction(value);
        // return linearFunction(value);
        // return discretizingFunction(value);
        return scaleForContextRange(value);

    }

    private Double linearFunction(Double value) {
        // return 2 * (4 + Math.sqrt((value) / 10));
        return (1 + Math.sqrt((value)));
    }

    private Double logFunction(Double value) {
        return 4 + Math.log(value) * 10;
    }

    private Double discretizingFunction(Double value) {
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
