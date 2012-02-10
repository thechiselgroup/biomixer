/*******************************************************************************
 * Copyright 2012 David Rusk 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import java.util.Arrays;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;
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

        SizeInt displayArea = callback.getDisplayArea();

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
