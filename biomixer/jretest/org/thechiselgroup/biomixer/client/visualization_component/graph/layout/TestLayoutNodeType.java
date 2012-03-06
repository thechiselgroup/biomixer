/*******************************************************************************
 * Copyright 2012 David Rusk, Lars Grammel 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TestLayoutNodeType implements LayoutNodeType {

    Set<LayoutNode> nodes = new HashSet<LayoutNode>();

    public void add(LayoutNode node) {
        nodes.add(node);
    }

    public void addAll(Collection<LayoutNode> nodeCollection) {
        nodes.addAll(nodeCollection);
    }

    @Override
    public Set<LayoutNode> getNodes() {
        return nodes;
    }

}
