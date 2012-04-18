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
import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.executor.DelayedExecutor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;

public class ForceDirectedLayoutAlgorithm implements LayoutAlgorithm {

    protected static final int DELAY_BETWEEN_ITERATIONS = 250;

    private ErrorHandler errorHandler;

    private DelayedExecutor executor = new DelayedExecutor(
            DELAY_BETWEEN_ITERATIONS);

    private final double damping;

    private ForceCalculator forceCalculator;

    private AnimationRunner animationRunner;

    /**
     * 
     * @param forceCalculator
     *            determines what forces are applied to nodes
     * @param damping
     *            coefficient between 0 and 1
     * @param errorHandler
     */
    public ForceDirectedLayoutAlgorithm(ForceCalculator forceCalculator,
            double damping, AnimationRunner animationRunner,
            ErrorHandler errorHandler) {
        this.forceCalculator = forceCalculator;
        this.damping = damping;
        this.errorHandler = errorHandler;
        this.animationRunner = animationRunner;
    }

    @Override
    public LayoutComputation computeLayout(LayoutGraph graph) {
        ForceDirectedLayoutComputation computation = new ForceDirectedLayoutComputation(
                forceCalculator, damping, graph, executor, errorHandler,
                animationRunner, DELAY_BETWEEN_ITERATIONS);
        computation.run();
        return computation;
    }

}
