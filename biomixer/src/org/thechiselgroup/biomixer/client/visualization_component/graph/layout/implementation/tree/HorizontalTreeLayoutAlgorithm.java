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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.executor.DirectExecutor;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

public class HorizontalTreeLayoutAlgorithm extends AbstractLayoutAlgorithm {

    private Executor executor = new DirectExecutor();

    private ErrorHandler errorHandler;

    private final boolean pointingRight;

    private final NodeAnimator nodeAnimator;

    /**
     * 
     * @param pointingRight
     *            if <code>true</code>, arrows on directed arcs will be pointing
     *            to the right. If <code>false</code> they will point to the
     *            left.
     */
    public HorizontalTreeLayoutAlgorithm(boolean pointingRight,
            ErrorHandler errorHandler, NodeAnimator nodeAnimator) {
        this.errorHandler = errorHandler;
        this.pointingRight = pointingRight;
        this.nodeAnimator = nodeAnimator;
    }

    @Override
    protected AbstractLayoutComputation getLayoutComputation(LayoutGraph graph) {
        return new HorizontalTreeLayoutComputation(graph, executor,
                errorHandler, nodeAnimator, pointingRight);
    }

}
