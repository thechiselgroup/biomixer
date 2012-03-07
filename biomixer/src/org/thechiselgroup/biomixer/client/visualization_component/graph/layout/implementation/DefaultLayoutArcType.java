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

import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArcType;

/**
 * An arc type corresponding ot one of the string based types passed into the
 * graph.
 * 
 * @author drusk
 * 
 */
public class DefaultLayoutArcType implements LayoutArcType, Identifiable {

    private String typeId;

    private List<LayoutArc> arcs = new ArrayList<LayoutArc>();

    public DefaultLayoutArcType(String typeId) {
        this.typeId = typeId;
    }

    public void add(LayoutArc arc) {
        arcs.add(arc);
    }

    @Override
    public List<LayoutArc> getArcs() {
        return arcs;
    }

    @Override
    public String getId() {
        return typeId;
    }

}
