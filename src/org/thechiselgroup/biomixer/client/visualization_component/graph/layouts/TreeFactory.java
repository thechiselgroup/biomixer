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
