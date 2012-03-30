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

/**
 * Layout requests that come in before the previous finishes are dealt with by
 * telling the current computation to stop and scheduling a brand new one.
 * 
 * @author drusk
 * 
 */
public class RestartingLayoutExecutionManager implements
        GraphLayoutExecutionManager {

    private LayoutAlgorithm layoutAlgorithm;

    private LayoutComputation currentComputation = null;

    private LayoutGraph graph;

    /*
     * true if another call to runLayout has already told the current
     * computation to stop and run a new one
     */
    private boolean hasExistingRestartRequest = false;

    public RestartingLayoutExecutionManager(LayoutAlgorithm layoutAlgorithm,
            LayoutGraph graph) {
        this.layoutAlgorithm = layoutAlgorithm;
        this.graph = graph;
    }

    /**
     * Tell the current computation to stop and schedule a new one once it is
     * done.
     */
    private void requestRestart() {
        hasExistingRestartRequest = true;

        currentComputation
                .addEventHandler(new LayoutComputationFinishedHandler() {

                    @Override
                    public void onLayoutComputationFinished(
                            LayoutComputationFinishedEvent e) {
                        /*
                         * Run a new computation, clear existing requests
                         */
                        currentComputation = layoutAlgorithm
                                .computeLayout(graph);
                        hasExistingRestartRequest = false;
                    }
                });

        currentComputation.stop();
    }

    /**
     * Schedule the layout algorithm to start a new computation as soon as
     * possible.
     */
    @Override
    public void runLayout() {
        if (currentComputation == null || !currentComputation.isRunning()) {
            currentComputation = layoutAlgorithm.computeLayout(graph);
        } else if (!hasExistingRestartRequest) {
            requestRestart();
        }
    }
}
