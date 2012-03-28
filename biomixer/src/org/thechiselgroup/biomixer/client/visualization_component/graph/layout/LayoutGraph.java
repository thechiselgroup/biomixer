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

import java.util.List;

/**
 * 
 * @author Lars Grammel
 */
public interface LayoutGraph {

    /**
     * NOTE: we return a list to guarantee arc order for testing purposes. There
     * must be no duplicate arcs in this list.
     * 
     * @return all arcs (from all arc types) in this graph
     */
    List<LayoutArc> getAllArcs();

    /**
     * NOTE: we return a list to guarantee node order for testing purposes.
     * There must be no duplicate nodes in this list.
     * 
     * @return all nodes (from all node types) in this graph
     */
    List<LayoutNode> getAllNodes();

    /**
     * NOTE: we return a list to guarantee arc order for testing purposes. There
     * must be no duplicate arcs in this list.
     * 
     * @return all arc types in this graph
     */
    List<LayoutArcType> getArcTypes();

    /**
     * @return bounds of the canvas rectangle
     */
    BoundsDouble getBounds();

    /**
     * 
     * @return the farthest left point of a node on a graph.
     */
    double getLeftMostNodeX();

    /**
     * NOTE: we return a list to guarantee node order for testing purposes.
     * There must be no duplicate nodes in this list.
     * 
     * @return all node types in this graph
     */
    List<LayoutNodeType> getNodeTypes();

    /**
     * 
     * @return the point of a node closest to the top of the graph.
     */
    double getTopMostNodeY();

}