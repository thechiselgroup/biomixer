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
package org.thechiselgroup.biomixer.client.visualization_component.graph.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NodeElementList implements Iterable<NodeElement> {

    private List<NodeElement> nodeElements;

    public NodeElementList() {
        nodeElements = new ArrayList<NodeElement>();
    }

    public NodeElementList(List<NodeElement> nodeElements) {
        this.nodeElements = nodeElements;
    }

    public void add(NodeElement nodeElement) {
        nodeElements.add(nodeElement);
    }

    public boolean containsNodeWithId(String id) {
        for (NodeElement nodeElement : nodeElements) {
            if (nodeElement.getNodeId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public NodeElement getNodeElement(String id) {
        for (NodeElement nodeElement : nodeElements) {
            if (nodeElement.getNodeId().equals(id)) {
                return nodeElement;
            }
        }
        return null;
    }

    @Override
    public Iterator<NodeElement> iterator() {
        return nodeElements.iterator();
    }

    public void remove(String nodeId) {
        nodeElements.remove(getNodeElement(nodeId));
    }

}
