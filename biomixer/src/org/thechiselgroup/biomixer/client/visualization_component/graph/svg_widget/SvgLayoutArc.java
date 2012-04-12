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

import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.ArcSvgComponent;

public class SvgLayoutArc implements LayoutArc, Identifiable {

    private ArcSvgComponent svgComponent;

    private final LayoutArcType arcType;

    public SvgLayoutArc(ArcSvgComponent svgComponent, LayoutArcType arcType) {
        this.svgComponent = svgComponent;
        this.arcType = arcType;
    }

    @Override
    public String getId() {
        return svgComponent.getId();
    }

    public ArcSvgComponent getRenderedArc() {
        return svgComponent;
    }

    @Override
    public LayoutNode getSourceNode() {
        return svgComponent.getSourceNode();
    }

    @Override
    public LayoutNode getTargetNode() {
        return svgComponent.getTargetNode();
    }

    @Override
    public double getThickness() {
        return svgComponent.getThickness();
    }

    @Override
    public LayoutArcType getType() {
        return arcType;
    }

    @Override
    public boolean isDirected() {
        return svgComponent.isDirected();
    }

}
