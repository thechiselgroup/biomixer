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

/**
 * Arc that is part of the graph that should be laid out. The arcs will be
 * automatically drawn as straight lines between the source and the target
 * nodes.
 * 
 * @author Lars Grammel
 */
public interface LayoutArc {

    /**
     * @return source node of this arc
     */
    LayoutNode getSourceNode();

    /**
     * @return target node of this arc
     */
    LayoutNode getTargetNode();

    /**
     * @return thickness of this arc
     */
    double getThickness();

    /**
     * @return type of this arc
     */
    LayoutArcType getType();

    /**
     * @return <code>true</code> if the arc direction is important (e.g. an
     *         indicator of the direction will be displayed). A directed node
     *         point from source - {@link #getSourceNode()} - to target -
     *         {@link #getTargetNode()}.
     */
    boolean isDirected();

}