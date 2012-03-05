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

import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;

/**
 * Node that is part of the graph that should be laid out.
 * 
 * TODO the label issue (representing labels as being separate from graph nodes
 * and arcs) is not considered in this interface specification. Once we have a
 * need for considering separate labels for arcs and nodes, the specification
 * should be extended.
 * 
 * @author Lars Grammel
 */
public interface LayoutNode {

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
     * @return <code>true</code> when the node should not be moved.
     */
    boolean isAnchored();

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