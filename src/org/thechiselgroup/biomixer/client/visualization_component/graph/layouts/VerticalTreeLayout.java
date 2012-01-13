package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import java.util.Arrays;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.geometry.Size;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayout;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;

public class VerticalTreeLayout implements GraphLayout {

    @Override
    public void run(NodeItem[] nodes, ArcItem[] arcs,
            GraphLayoutCallback callback) {

        List<Tree> treesOnGraph = new TreeFactory().getTrees(
                Arrays.asList(nodes), Arrays.asList(arcs));
        assert treesOnGraph.size() >= 1;

        Size displayArea = callback.getDisplayArea();

        // XXX if there is more than one tree this will give them each the same
        // width to work with. It might be better to take into account the
        // widths of the trees as well.
        int availableWidthForEachTree = displayArea.getWidth()
                / treesOnGraph.size();

        for (int i = 0; i < treesOnGraph.size(); i++) {
            Tree tree = treesOnGraph.get(i);

            int verticalSpacing = displayArea.getHeight()
                    / (tree.getHeight() + 1);
            int currentY = verticalSpacing;

            for (int j = 0; j < tree.getHeight(); j++) {
                List<TreeNode> nodesAtDepth = tree.getNodesAtDepth(j);

                int horizontalSpacing = availableWidthForEachTree
                        / (nodesAtDepth.size() + 1);

                int currentX = i * availableWidthForEachTree
                        + horizontalSpacing;
                for (TreeNode treeNode : nodesAtDepth) {
                    callback.setLocation(treeNode.getNodeItem(), new Point(
                            currentX, currentY));
                    currentX += horizontalSpacing;
                }

                currentY += verticalSpacing;
            }

        }

    }
}
