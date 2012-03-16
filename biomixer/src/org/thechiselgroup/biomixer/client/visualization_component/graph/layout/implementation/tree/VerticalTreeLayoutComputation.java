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
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.LayoutUtils;

public class VerticalTreeLayoutComputation extends
        AbstractTreeLayoutComputation {

    protected VerticalTreeLayoutComputation(LayoutGraph graph,
            Executor executor, ErrorHandler errorHandler, boolean pointingUp) {
        super(graph, executor, errorHandler, !pointingUp);
    }

    @Override
    protected double getAvailableSecondaryDimensionForEachTree(
            int numDagsOnGraph) {
        return graph.getBounds().getWidth() / numDagsOnGraph;
    }

    @Override
    protected double getPrimaryDimensionSpacing(int numberOfNodesOnLongestPath) {
        return graph.getBounds().getHeight() / (numberOfNodesOnLongestPath + 1);
    }

    @Override
    protected PointDouble getTopLeftForCentreAt(double currentPrimaryDimension,
            double currentSecondaryDimension, LayoutNode node) {
        return LayoutUtils.getTopLeftForCentreAt(currentSecondaryDimension,
                currentPrimaryDimension, node);
    }

}
