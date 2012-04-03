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

public class TestLayoutArc implements LayoutArc {

    private TestLayoutNode sourceNode;

    private TestLayoutNode targetNode;

    private double thickness;

    private boolean isDirected;

    private LayoutArcType type;

    public TestLayoutArc(TestLayoutNode sourceNode, TestLayoutNode targetNode,
            double thickness, boolean isDirected, LayoutArcType type) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.thickness = thickness;
        this.isDirected = isDirected;
        this.type = type;
        sourceNode.addConnectedArc(this);
        targetNode.addConnectedArc(this);
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
        return thickness;
    }

    @Override
    public LayoutArcType getType() {
        return type;
    }

    @Override
    public boolean isDirected() {
        return isDirected;
    }

}
