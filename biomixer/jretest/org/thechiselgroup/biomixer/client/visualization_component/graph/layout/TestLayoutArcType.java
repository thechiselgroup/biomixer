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

public class TestLayoutArcType implements LayoutArcType {

    private Set<LayoutArc> arcs = new HashSet<LayoutArc>();

    public void add(LayoutArc arc) {
        arcs.add(arc);
    }

    public void addAll(Collection<LayoutArc> arcCollection) {
        arcs.addAll(arcCollection);
    }

    @Override
    public Set<LayoutArc> getArcs() {
        return arcs;
    }

}
