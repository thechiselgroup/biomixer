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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.circle;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.executor.DirectExecutor;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

public class CircleLayoutAlgorithm implements LayoutAlgorithm {

    private Executor executor = new DirectExecutor();

    private final ErrorHandler errorHandler;

    private double minAngle = 0.0;

    private double maxAngle = 360.0;

    private final AnimationRunner animationRunner;

    public CircleLayoutAlgorithm(ErrorHandler errorHandler,
            AnimationRunner animationRunner) {
        this.errorHandler = errorHandler;
        this.animationRunner = animationRunner;
    }

    @Override
    public LayoutComputation computeLayout(LayoutGraph graph) {
        AbstractLayoutComputation computation = new CircleLayoutComputation(
                minAngle, maxAngle, graph, executor, errorHandler,
                animationRunner);
        computation.run();
        return computation;
    }

    public void setAngleRange(double angle1, double angle2) {
        if (angle1 < angle2) {
            minAngle = angle1;
            maxAngle = angle2;
        } else {
            maxAngle = angle1;
            minAngle = angle2;
        }
        assert maxAngle - minAngle <= 360.0;
    }

}
