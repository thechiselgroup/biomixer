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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputationFinishedEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputationFinishedHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;

/**
 * Runs a layout on a specific node, anchoring the others. If a layout is
 * already in progress
 * 
 * @author drusk
 * 
 */
public class SelectedNodeLayoutExecutionManager {

    private LayoutAlgorithm layoutAlgorithm;

    private LayoutComputation currentComputation = null;

    private LayoutGraph graph;

    public SelectedNodeLayoutExecutionManager(LayoutAlgorithm layoutAlgorithm,
            LayoutGraph graph) {
        this.layoutAlgorithm = layoutAlgorithm;
        this.graph = graph;
    }

    public void runLayout(final LayoutNode node) {
        if (currentComputation == null || !currentComputation.isRunning()) {
            /*
             * Anchor all other nodes, then run a brand new computation.
             */
            for (LayoutNode otherNode : graph.getNodesExcept(node)) {
                otherNode.setAnchored(true);
            }
            currentComputation = layoutAlgorithm.computeLayout(graph);
            currentComputation
                    .addEventHandler(new LayoutComputationFinishedHandler() {

                        @Override
                        public void onLayoutComputationFinished(
                                LayoutComputationFinishedEvent e) {
                            for (LayoutNode otherNode : graph
                                    .getNodesExcept(node)) {
                                otherNode.setAnchored(false);
                            }

                        }
                    });
        }

        /*
         * Otherwise, take no action.
         */
    }

}
