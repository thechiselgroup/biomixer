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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.executor.DirectExecutor;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;

public class ForceDirectedLayoutAlgorithm implements LayoutAlgorithm {

    private ErrorHandler errorHandler;

    private Executor executor = new DirectExecutor();

    private final double timeStep;

    private final double damping;

    private ForceCalculator forceCalculator;

    public ForceDirectedLayoutAlgorithm(ForceCalculator forceCalculator,
            double timeStep, double damping, ErrorHandler errorHandler) {
        this.forceCalculator = forceCalculator;
        this.timeStep = timeStep;
        this.damping = damping;
        this.errorHandler = errorHandler;
    }

    @Override
    public LayoutComputation computeLayout(LayoutGraph graph) {
        ForceDirectedLayoutComputation computation = new ForceDirectedLayoutComputation(
                forceCalculator, timeStep, damping, graph, executor,
                errorHandler);
        computation.run();
        return computation;
    }

}
