/*******************************************************************************
 * Copyright 2012 Lars Grammel, David Rusk 
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

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;

/**
 * Node that is part of the graph that should be laid out. Each node can have an
 * optional separate label (which has its own size and can be positioned as
 * well).
 * 
 * @author Lars Grammel
 */
public interface LayoutNode {

    /**
     * @return size of the separate node label. Returns a SizeDouble(0,0) if
     *         there is no label.
     */
    SizeDouble getLabelSize();

    /**
     * @return current x position of the node label
     */
    double getLabelX();

    /**
     * @return current y position of the node label
     */
    double getLabelY();

    /**
     * @return size of this node
     */
    SizeDouble getSize();

    /**
     * Returns the type of this node.
     */
    LayoutNodeType getType();

    /**
     * @return current x position of this node, or Double.NaN if undefined.
     */
    double getX();

    /**
     * @return current y position of this node, or Double.NaN if undefined.
     */
    double getY();

    /**
     * @return <code>true</code> if the node has a separate label (if the label
     *         is part of the node, this method returns false).
     */
    boolean hasLabel();

    /**
     * @return <code>true</code> when the node should not be moved.
     */
    boolean isAnchored();

    /**
     * Sets the position of the node label.
     * 
     * @param x
     *            left starting point of the label
     * @param y
     *            top starting point of the label
     */
    void setLabelPosition(double x, double y);

    /**
     * Sets the x-position of the node label.
     * 
     * @param x
     *            left starting point of the label
     */
    void setLabelX(double x);

    /**
     * Sets the y-position of the node label.
     * 
     * @param y
     *            top starting point of the label
     */
    void setLabelY(double y);

    /**
     * Sets the position of this node. Only nodes that are not anchored can be
     * positioned (can lead to assertion error otherwise).
     * 
     * @param x
     *            left starting point of the node
     * @param y
     *            top starting point of the node
     */
    void setPosition(double x, double y);

    /**
     * Sets the position of this node. Only nodes that are not anchored can be
     * positioned (can lead to assertion error otherwise).
     * 
     * @param position
     *            the top left corner of the node
     */
    void setPosition(PointDouble position);

    /**
     * Sets the x-position of this node. Only nodes that are not anchored can be
     * positioned (can lead to assertion error otherwise).
     * 
     * @param x
     *            left starting point of the node
     */
    void setX(double x);

    /**
     * Sets the y-position of this node. Only nodes that are not anchored can be
     * positioned (can lead to assertion error otherwise).
     * 
     * @param y
     *            top starting point of the node
     */
    void setY(double y);

}