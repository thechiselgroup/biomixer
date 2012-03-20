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

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

public abstract class AbstractLayoutGraph implements LayoutGraph {

    @Override
    public List<LayoutArc> getConnectedArcs(LayoutNode layoutNode) {
        List<LayoutArc> connectedArcs = new ArrayList<LayoutArc>();
        for (LayoutArc layoutArc : getAllArcs()) {
            if (layoutArc.getSourceNode().equals(layoutNode)
                    || layoutArc.getTargetNode().equals(layoutNode)) {
                connectedArcs.add(layoutArc);
            }
        }
        return connectedArcs;
    }

}
