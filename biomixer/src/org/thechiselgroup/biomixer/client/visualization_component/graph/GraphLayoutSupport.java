/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphLayouts;

public interface GraphLayoutSupport {

    // TODO remove
    LayoutGraph getLayoutGraph();

    /**
     * Registers a layout algorithm to be the default one run when the graph
     * changes.
     * 
     * @param layoutAlgorithm
     */
    void registerDefaultLayout(LayoutAlgorithm layoutAlgorithm);

    /**
     * Runs the current default layout.
     */
    void runLayout();

    /**
     * Runs a customized graph layout.
     */
    // TODO remove
    void runLayout(GraphLayout layout);

    /**
     * 
     * @param layoutAlgorithm
     *            the layout algorithm to be run
     */
    void runLayout(LayoutAlgorithm layoutAlgorithm);

    /**
     * Runs a graph layout that is defined in the graph widget. The constants
     * are contained in {@link GraphLayouts}.
     * 
     * @see GraphLayouts
     */
    // TODO remove
    void runLayout(String layout);
}