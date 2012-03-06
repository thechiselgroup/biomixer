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

import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;

public class TestLayoutNode implements LayoutNode {

    private class NodeSize implements SizeDouble {

        private final double width;

        private final double height;

        public NodeSize(double width, double height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public double getHeight() {
            return height;
        }

        @Override
        public double getWidth() {
            return width;
        }

    }

    private double x;

    private double y;

    private boolean isAnchored;

    private LayoutNodeType type;

    private NodeSize nodeSize;

    public TestLayoutNode(double width, double height, boolean isAnchored,
            LayoutNodeType type) {
        this.nodeSize = new NodeSize(width, height);
        this.isAnchored = isAnchored;
        this.type = type;
    }

    @Override
    public SizeDouble getSize() {
        return nodeSize;
    }

    @Override
    public LayoutNodeType getType() {
        return type;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public boolean isAnchored() {
        return isAnchored;
    }

    @Override
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

}
