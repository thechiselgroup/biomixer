package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;

public class TreeFactory {

    public static TreeFactory getInstance() {
        return new TreeFactory();
    }

    private final Map<NodeItem, List<NodeItem>> childrenOfNode;

    public TreeFactory() {
        this.childrenOfNode = new HashMap<NodeItem, List<NodeItem>>();
    }

    public void addChildren(TreeNode node) {
        if (!childrenOfNode.containsKey(node.getNodeItem())) {
            // Reached a leaf
            return;
        }

        node.addChildren(childrenOfNode.get(node.getNodeItem()));

        for (TreeNode childNode : node.getChildren()) {
            addChildren(childNode);
        }
    }

    private NodeItem getNodeById(NodeItem[] nodes, String sourceNodeId) {
        for (NodeItem nodeItem : nodes) {
            if (nodeItem.getNode().getId().equals(sourceNodeId)) {
                return nodeItem;
            }
        }
        return null;
    }

    public List<Tree> getTrees(NodeItem[] nodes, ArcItem[] arcs) {
        List<NodeItem> notChildren = new ArrayList<NodeItem>();
        notChildren.addAll(Arrays.asList(nodes));

        // 1. Iterate through arcs
        for (ArcItem arcItem : arcs) {
            // 2. Build up Mapping of nodes to their list of child nodes
            Arc arc = arcItem.getArc();

            NodeItem sourceNodeItem = getNodeById(nodes, arc.getSourceNodeId());
            NodeItem targetNodeItem = getNodeById(nodes, arc.getTargetNodeId());

            if (!childrenOfNode.containsKey(sourceNodeItem)) {
                childrenOfNode.put(sourceNodeItem, new ArrayList<NodeItem>());
            }
            childrenOfNode.get(sourceNodeItem).add(targetNodeItem);

            // 3. Keep record of nodes that have appeared in other nodes' child
            // list of children
            notChildren.remove(targetNodeItem);
        }

        // 4. Those that are in no child lists are the roots. Starting at
        // root(s) build up trees
        List<Tree> trees = new ArrayList<Tree>();
        for (NodeItem rootNode : notChildren) {
            // 5. Create new tree.
            Tree tree = new Tree(rootNode);
            tree.getRoot().addChildren(childrenOfNode.get(rootNode));
            for (TreeNode node : tree.getRoot().getChildren()) {
                addChildren(node);
            }
            trees.add(tree);
        }

        return trees;
    }

}
