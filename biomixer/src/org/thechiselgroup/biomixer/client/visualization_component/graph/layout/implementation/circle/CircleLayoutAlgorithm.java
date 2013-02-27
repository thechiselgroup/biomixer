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
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

public class CircleLayoutAlgorithm extends AbstractLayoutAlgorithm {

    protected double minAngle = 0.0;

    protected double maxAngle = 360.0;

    public CircleLayoutAlgorithm(ErrorHandler errorHandler,
            NodeAnimator nodeAnimator) {
        super(errorHandler, nodeAnimator);
    }

    @Override
    protected AbstractLayoutComputation getLayoutComputation(LayoutGraph graph) {
        return new CircleLayoutComputation(minAngle, maxAngle, graph, executor,
                errorHandler, nodeAnimator);
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
