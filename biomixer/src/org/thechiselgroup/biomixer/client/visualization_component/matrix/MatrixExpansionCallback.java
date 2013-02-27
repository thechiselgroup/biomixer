package org.thechiselgroup.biomixer.client.visualization_component.matrix;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;

public class MatrixExpansionCallback extends NodeExpansionCallback<NeoD3Matrix> {

    private NeoD3Matrix matrix;

    MatrixExpansionCallback(NeoD3Matrix matrix) {
        this.matrix = matrix;
    }
    
    @Override
    public NeoD3Matrix getDisplay() {
        return this.matrix;
    }

    @Override
    public LightweightCollection<VisualItem> getVisualItems(
            Iterable<Resource> resources) {
        return this.matrix.getVisualItems(resources);
    }

    @Override
    public boolean isInitialized() {
        return this.matrix.isInitialized();
    }

    @Override
    public boolean isRestoring() {
        return this.matrix.isRestoring();
    }

    @Override
    public void updateArcsForResources(Iterable<Resource> resources) {
        // This calls the method that triggers updates in D3,
        // after it has added the new mappings
        this.matrix.updateView();
    }

    @Override
    public void updateArcsForVisuaItems(
            LightweightCollection<VisualItem> visualItems) {
    	// This calls the method that triggers updates in D3,
        // after it has added the new mappings
        this.matrix.updateView();
    }
}