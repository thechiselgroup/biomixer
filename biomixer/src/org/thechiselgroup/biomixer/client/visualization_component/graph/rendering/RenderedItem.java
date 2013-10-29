package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering;

import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

public interface RenderedItem {

    /**
     * More convenient than needing a reference to the Graph object in order to
     * get at the underlying model with the similarly named method foudn there.
     * 
     * @return
     */
    public VisualItem getVisualItem();

}
