package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

public class TreeFactory {

    public List<Tree> getTrees(LayoutGraph graph) {
        Map<LayoutNode, TreeNode> treeNodes = new HashMap<LayoutNode, TreeNode>();
        List<TreeNode> potentialRoots = new ArrayList<TreeNode>();
        for (LayoutNode node : graph.getAllNodes()) {
            TreeNode root = new TreeNode(node);
            treeNodes.put(node, root);
            potentialRoots.add(root);
        }

        for (LayoutArc arc : graph.getAllArcs()) {
            // XXX arcs point from child to parent. Therefore sourceNode is a
            // child of targetNode.
            TreeNode sourceNode = treeNodes.get(arc.getSourceNode());
            TreeNode targetNode = treeNodes.get(arc.getTargetNode());

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
