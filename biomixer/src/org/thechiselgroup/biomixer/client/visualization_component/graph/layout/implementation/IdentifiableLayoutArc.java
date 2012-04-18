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

import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;

public class IdentifiableLayoutArc implements LayoutArc, Identifiable {

    private RenderedArc renderedArc;

    private final LayoutArcType arcType;

    private final LayoutNode sourceNode;

    private final LayoutNode targetNode;

    private final String id;

    public IdentifiableLayoutArc(String id, RenderedArc renderedArc,
            LayoutArcType arcType, LayoutNode sourceNode, LayoutNode targetNode) {
        this.id = id;
        this.renderedArc = renderedArc;
        this.arcType = arcType;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }

    @Override
    public String getId() {
        return id;
    }

    public RenderedArc getRenderedArc() {
        return renderedArc;
    }

    @Override
    public LayoutNode getSourceNode() {
        return sourceNode;
    }

    @Override
    public LayoutNode getTargetNode() {
        return targetNode;
    }

    @Override
    public double getThickness() {
        return renderedArc.getThickness();
    }

    @Override
    public LayoutArcType getType() {
        return arcType;
    }

    @Override
    public boolean isDirected() {
        return renderedArc.isDirected();
    }

}
