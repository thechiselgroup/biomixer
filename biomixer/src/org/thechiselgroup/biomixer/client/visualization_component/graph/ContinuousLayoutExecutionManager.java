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
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;

/**
 * Layout requests that come in before the previous finishes are ignored. With
 * each iteration the computation is expected to check for any changes on the
 * graph and take them into account.
 * 
 * @author drusk
 * 
 */
public class ContinuousLayoutExecutionManager implements
        GraphLayoutExecutionManager {

    private LayoutAlgorithm layoutAlgorithm;

    private LayoutComputation currentComputation = null;

    private LayoutGraph graph;

    public ContinuousLayoutExecutionManager(LayoutAlgorithm layoutAlgorithm,
            LayoutGraph graph) {
        this.layoutAlgorithm = layoutAlgorithm;
        this.graph = graph;
    }

    @Override
    public void runLayout() {
        if (currentComputation == null) {
            /*
             * Run a brand new computation.
             */
            currentComputation = layoutAlgorithm.computeLayout(graph);
        } else if (!currentComputation.isRunning()) {
            /*
             * Restart with previous state
             */
            currentComputation.restart();
        }

        /*
         * Otherwise, take no action.
         */
    }

}
