package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.ForceDirectedLayoutAlgorithm;

/**
 * This embed is just for testing layouts through visual inspection.
 * 
 * @author drusk
 * 
 */
public class TestLayoutLoader extends ConceptNeighbourhoodLoader {

    public static final String EMBED_MODE = "test";

    @Override
    public String getEmbedMode() {
        return EMBED_MODE;
    }

    @Override
    protected void setLayoutAlgorithm() {
        this.layoutAlgorithm = new ForceDirectedLayoutAlgorithm(errorHandler);
    }

}
