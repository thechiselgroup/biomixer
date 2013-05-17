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
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.IdentifiableLayoutGraph;

public class CircleLayoutWithCentralNodeAlgorithm extends CircleLayoutAlgorithm {

    private String centralNodeUri;

    /**
     * 
     * @param errorHandler
     * @param nodeAnimator
     * @param centralNodeUri
     *            If null, selects the node with the most incoming arcs
     */
    public CircleLayoutWithCentralNodeAlgorithm(ErrorHandler errorHandler,
            NodeAnimator nodeAnimator, String centralNodeUri) {
        super(errorHandler, nodeAnimator);
        this.centralNodeUri = centralNodeUri;
    }

    @Override
    protected AbstractLayoutComputation getLayoutComputation(LayoutGraph graph) {
        return new CircleLayoutWithCentralNodeComputation(minAngle, maxAngle,
                (IdentifiableLayoutGraph) graph, executor, errorHandler,
                nodeAnimator, centralNodeUri);
    }
}
