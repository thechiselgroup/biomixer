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
import java.util.Set;

public class Tree {

    private final TreeNode root;

    private Map<Integer, List<TreeNode>> nodesByDepth = new HashMap<Integer, List<TreeNode>>();

    public Tree(TreeNode root) {
        this.root = root;
        initializeTreeMapping();
    }

    public Set<TreeNode> getAllNodes() {
        Set<TreeNode> allNodes = root.getDescendants();
        allNodes.add(root);
        return allNodes;
    }

    public int getHeight() {
        return root.getHeight();
    }

    private int getMaxDepth(TreeNode treeNode) {
        return root.getMaxDistance(treeNode);
    }

    /**
     * 
     * @param depth
     *            : 0 -> height - 1
     * @return Find all nodes at the specified depth from the root
     */
    public List<TreeNode> getNodesAtDepth(int depth) {
        assert depth >= 0;
        // return root.getNodesFromLevelsBelow(depth);
        assert nodesByDepth.containsKey(Integer.valueOf(depth));
        return nodesByDepth.get(Integer.valueOf(depth));
    }

    public TreeNode getRoot() {
        return root;
    }

    private void initializeTreeMapping() {
        Set<TreeNode> allNodes = getAllNodes();
        for (TreeNode treeNode : allNodes) {
            Integer maxDepth = Integer.valueOf(getMaxDepth(treeNode));
            if (!nodesByDepth.containsKey(maxDepth)) {
                nodesByDepth.put(maxDepth, new ArrayList<TreeNode>());
            }
            nodesByDepth.get(maxDepth).add(treeNode);
        }
    }

    public int size() {
        return root.getNumberOfDescendants() + 1;
    }

}
