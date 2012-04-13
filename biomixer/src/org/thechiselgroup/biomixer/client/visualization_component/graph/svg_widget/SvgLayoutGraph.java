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
package org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget;

import org.thechiselgroup.biomixer.client.core.util.collections.IdentifiablesList;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.DefaultLayoutGraph;

public class SvgLayoutGraph extends DefaultLayoutGraph {

    private IdentifiablesList<SvgLayoutNode> svgLayoutNodes = new IdentifiablesList<SvgLayoutNode>();

    private IdentifiablesList<SvgLayoutArc> svgLayoutArcs = new IdentifiablesList<SvgLayoutArc>();

    public SvgLayoutGraph(double width, double height) {
        super(width, height);
    }

    public void addSvgLayoutArc(SvgLayoutArc arc) {
        addLayoutArc(arc);
        svgLayoutArcs.add(arc);
    }

    public void addSvgLayoutNode(SvgLayoutNode node) {
        addLayoutNode(node);
        svgLayoutNodes.add(node);
    }

    public SvgLayoutArc getSvgLayoutArc(String id) {
        return svgLayoutArcs.get(id);
    }

    public SvgLayoutNode getSvgLayoutNode(String id) {
        return svgLayoutNodes.get(id);
    }

    public void removeSvgLayoutArc(String id) {
        removeLayoutArc(getSvgLayoutArc(id));
        svgLayoutArcs.remove(id);
    }

    public void removeSvgLayoutNode(String id) {
        removeLayoutNode(getSvgLayoutNode(id));
        svgLayoutNodes.remove(id);
    }

}
