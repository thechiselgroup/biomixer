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

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;

public class TestLayoutNode implements LayoutNode {

    private double x = Double.NaN;

    private double y = Double.NaN;

    private double labelX = Double.NaN;

    private double labelY = Double.NaN;

    private boolean hasLabel;

    private boolean isAnchored;

    private LayoutNodeType type;

    private DefaultSizeDouble nodeSize;

    private DefaultSizeDouble labelSize;

    List<LayoutArc> connectedArcs = new ArrayList<LayoutArc>();

    public TestLayoutNode(double width, double height, boolean isAnchored,
            double labelWidth, double LabelHeight, LayoutNodeType type) {

        this.nodeSize = new DefaultSizeDouble(width, height);
        this.isAnchored = isAnchored;
        this.type = type;
        this.hasLabel = true;
        this.labelSize = new DefaultSizeDouble(labelWidth, LabelHeight);
    }

    public TestLayoutNode(double width, double height, boolean isAnchored,
            LayoutNodeType type) {

        this.nodeSize = new DefaultSizeDouble(width, height);
        this.isAnchored = isAnchored;
        this.type = type;
        this.hasLabel = false;
        this.labelSize = new DefaultSizeDouble(0, 0);
    }

    public void addConnectedArc(LayoutArc arc) {
        connectedArcs.add(arc);
    }

    @Override
    public PointDouble getCentre() {
        return new PointDouble(getX() + getSize().getWidth() / 2, getY()
                + getSize().getHeight() / 2);
    }

    @Override
    public List<LayoutArc> getConnectedArcs() {
        return connectedArcs;
    }

    @Override
    public SizeDouble getLabelSize() {
        return labelSize;
    }

    @Override
    public double getLabelX() {
        return labelX;
    }

    @Override
    public double getLabelY() {
        return labelY;
    }

    @Override
    public SizeDouble getSize() {
        return nodeSize;
    }

    @Override
    public PointDouble getTopLeftForCentreAt(double x, double y) {
        SizeDouble size = getSize();
        return new PointDouble(x - size.getWidth() / 2, y - size.getHeight()
                / 2);
    }

    @Override
    public PointDouble getTopLeftForCentreAt(PointDouble centre) {
        return getTopLeftForCentreAt(centre.getX(), centre.getY());
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
    public boolean hasLabel() {
        return hasLabel;
    }

    @Override
    public boolean isAnchored() {
        return isAnchored;
    }

    @Override
    public void setLabelPosition(double x, double y) {
        setLabelX(x);
        setLabelY(y);
    }

    @Override
    public void setLabelX(double x) {
        this.labelX = x;
    }

    @Override
    public void setLabelY(double y) {
        this.labelY = y;
    }

    @Override
    public void setPosition(double x, double y) {
        setX(x);
        setY(y);
    }

    @Override
    public void setPosition(PointDouble position) {
        setPosition(position.getX(), position.getY());
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
