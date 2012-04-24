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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputationFinishedHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;

public abstract class AbstractLayoutAlgorithm implements LayoutAlgorithm {

    @Override
    public LayoutComputation computeLayout(LayoutGraph graph) {
        return computeLayout(graph,
                new ArrayList<LayoutComputationFinishedHandler>());
    }

    @Override
    public LayoutComputation computeLayout(LayoutGraph graph,
            List<LayoutComputationFinishedHandler> handlers) {
        AbstractLayoutComputation computation = getLayoutComputation(graph);
        for (LayoutComputationFinishedHandler handler : handlers) {
            computation.addEventHandler(handler);
        }
        computation.run();
        return computation;
    }

    protected abstract AbstractLayoutComputation getLayoutComputation(
            LayoutGraph graph);

}
