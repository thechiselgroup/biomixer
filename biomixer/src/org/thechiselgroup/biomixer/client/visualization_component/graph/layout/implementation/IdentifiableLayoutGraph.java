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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.util.collections.IdentifiablesList;

import com.google.gwt.user.client.ui.Widget;

public class IdentifiableLayoutGraph extends DefaultLayoutGraph {

    private IdentifiablesList<IdentifiableLayoutNode> identifiableLayoutNodes = new IdentifiablesList<IdentifiableLayoutNode>();

    private IdentifiablesList<IdentifiableLayoutArc> identifiableLayoutArcs = new IdentifiablesList<IdentifiableLayoutArc>();

    private IdentifiablesList<DefaultLayoutNodeType> nodeTypes = new IdentifiablesList<DefaultLayoutNodeType>();

    private IdentifiablesList<DefaultLayoutArcType> arcTypes = new IdentifiablesList<DefaultLayoutArcType>();

    public IdentifiableLayoutGraph(Widget graphWidget, double width,
            double height) {
        super(graphWidget, width, height);
    }

    public void addIdentifiableLayoutArc(IdentifiableLayoutArc arc) {
        addLayoutArc(arc);
        identifiableLayoutArcs.add(arc);
    }

    public void addIdentifiableLayoutNode(IdentifiableLayoutNode node) {
        addLayoutNode(node);
        identifiableLayoutNodes.add(node);
    }

    @Override
    public void addLayoutArcType(DefaultLayoutArcType arcType) {
        arcTypes.add(arcType);
    }

    @Override
    public void addLayoutNodeType(DefaultLayoutNodeType nodeType) {
        nodeTypes.add(nodeType);
    }

    public boolean containsArcType(String arcType) {
        return arcTypes.contains(arcType);
    }

    public boolean containsNodeType(String nodeType) {
        return nodeTypes.contains(nodeType);
    }

    public IdentifiablesList<IdentifiableLayoutNode> getAllIdentifiableLayoutNodes() {
        return identifiableLayoutNodes;
    }

    public DefaultLayoutArcType getArcType(String arcType) {
        return arcTypes.get(arcType);
    }

    public IdentifiableLayoutArc getIdentifiableLayoutArc(String id) {
        return identifiableLayoutArcs.get(id);
    }

    public IdentifiableLayoutNode getIdentifiableLayoutNode(String id) {
        return identifiableLayoutNodes.get(id);
    }

    public List<String> getAllNodeIds() {
        List<String> nodeIds = new ArrayList<String>();
        for (IdentifiableLayoutNode node : identifiableLayoutNodes) {
            nodeIds.add(node.getId());
        }
        return nodeIds;
    }

    public DefaultLayoutNodeType getNodeType(String nodeType) {
        return nodeTypes.get(nodeType);
    }

    public void removeIdentifiableLayoutArc(String id) {
        removeLayoutArc(getIdentifiableLayoutArc(id));
        identifiableLayoutArcs.remove(id);
    }

    public void removeIdentifiableLayoutNode(String id) {
        removeLayoutNode(getIdentifiableLayoutNode(id));
        identifiableLayoutNodes.remove(id);
    }

    public void removeLayoutArcType(DefaultLayoutArcType arcType) {
        arcTypes.remove(arcType.getId());
    }

    public void removeLayoutNodeType(DefaultLayoutNodeType nodeType) {
        nodeTypes.remove(nodeType.getId());
    }

}
