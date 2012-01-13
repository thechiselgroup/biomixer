package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;

public class TreeFactory {

    private TreeNode getTreeNodeById(String id, List<TreeNode> allTreeNodes) {
        for (TreeNode treeNode : allTreeNodes) {
            if (treeNode.getNodeItem().getNode().getId().equals(id)) {
                return treeNode;
            }
        }
        return null;
    }

    public List<Tree> getTrees(NodeItem[] nodes, ArcItem[] arcs) {
        List<TreeNode> allTreeNodes = new ArrayList<TreeNode>();
        for (NodeItem nodeItem : nodes) {
            allTreeNodes.add(new TreeNode(nodeItem));
        }

        List<TreeNode> potentialRoots = new ArrayList<TreeNode>();
        potentialRoots.addAll(allTreeNodes);

        for (ArcItem arcItem : arcs) {
            Arc arc = arcItem.getArc();
            TreeNode sourceNode = getTreeNodeById(arc.getSourceNodeId(),
                    allTreeNodes);
            TreeNode targetNode = getTreeNodeById(arc.getTargetNodeId(),
                    allTreeNodes);

            sourceNode.addChild(targetNode);
            potentialRoots.remove(targetNode);
        }

        List<Tree> trees = new ArrayList<Tree>();
        for (TreeNode rootNode : potentialRoots) {
            trees.add(new Tree(rootNode));
        }
        return trees;
    }

}
