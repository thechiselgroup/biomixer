package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import java.util.List;

import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayout;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;

public class VerticalTreeLayout implements GraphLayout {

    @Override
    public void run(NodeItem[] nodes, ArcItem[] arcs,
            GraphLayoutCallback callback) {

        List<Tree> treesOnGraph = new TreeFactory().getTrees(nodes, arcs);

        // TODO: now that tree structures are reconstructed, use this
        // information to place each node

    }
}
