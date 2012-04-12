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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;

public class TreeFactory {

    public List<Tree> getTrees(List<NodeItem> nodes, List<ArcItem> arcs) {
        Map<String, TreeNode> treeNodesById = new HashMap<String, TreeNode>();
        for (NodeItem nodeItem : nodes) {
            treeNodesById.put(nodeItem.getNode().getId(),
                    new TreeNode(nodeItem));
        }

        List<TreeNode> potentialRoots = new ArrayList<TreeNode>();
        potentialRoots.addAll(treeNodesById.values());

        for (ArcItem arcItem : arcs) {
            Arc arc = arcItem.getArc();
            // XXX arcs point from child to parent. Therefore sourceNode is a
            // child of targetNode.
            TreeNode sourceNode = treeNodesById.get(arc.getSourceNodeId());
            TreeNode targetNode = treeNodesById.get(arc.getTargetNodeId());

            targetNode.addChild(sourceNode);
            potentialRoots.remove(sourceNode);
        }

        List<Tree> trees = new ArrayList<Tree>();
        for (TreeNode rootNode : potentialRoots) {
            trees.add(new Tree(rootNode));
        }
        return trees;
    }

}
