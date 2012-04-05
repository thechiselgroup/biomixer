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

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * Provides functionality common to all implementations of the
 * {@link LayoutGraph}.
 * 
 * @author drusk
 * 
 */
public abstract class AbstractLayoutGraph implements LayoutGraph {

    @Override
    public List<LayoutNode> getUnanchoredNodes() {
        List<LayoutNode> unanchoredNodes = new ArrayList<LayoutNode>();
        for (LayoutNode layoutNode : getAllNodes()) {
            if (!layoutNode.isAnchored()) {
                unanchoredNodes.add(layoutNode);
            }
        }
        return unanchoredNodes;
    }

}
