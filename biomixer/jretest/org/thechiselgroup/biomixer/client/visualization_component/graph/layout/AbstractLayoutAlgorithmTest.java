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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout;

import static org.junit.Assert.assertEquals;

import org.mockito.Mock;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.animation.NullAnimationRunner;

public abstract class AbstractLayoutAlgorithmTest extends
        AbstractLayoutGraphTest {

    private double delta = 0.1;

    @Mock
    protected ErrorHandler errorHandler;

    protected AnimationRunner animationRunner = new NullAnimationRunner();

    protected LayoutAlgorithm underTest;

    protected abstract void assertComputationRunningState(
            LayoutComputation computation);

    protected void assertNodeHasCentre(double x, double y, LayoutNode node) {
        SizeDouble nodeSize = node.getSize();
        assertEquals(x, node.getX() + nodeSize.getWidth() / 2, delta);
        assertEquals(y, node.getY() + nodeSize.getHeight() / 2, delta);
    }

    protected void assertNodesHaveCentreX(double x, LayoutNode... nodes) {
        for (LayoutNode layoutNode : nodes) {
            assertEquals(x, getCentreX(layoutNode), delta);
        }
    }

    protected void assertNodesHaveCentreY(double y, LayoutNode... nodes) {
        for (LayoutNode layoutNode : nodes) {
            assertEquals(y, getCentreY(layoutNode), delta);
        }
    }

    protected void computeLayout(TestLayoutGraph graph) {
        initNodePositions();
        LayoutComputation layoutComputation = underTest.computeLayout(graph);
        assertComputationRunningState(layoutComputation);
    }

    protected double getCentreX(LayoutNode layoutNode) {
        return layoutNode.getX() + layoutNode.getSize().getWidth() / 2;
    }

    protected double getCentreY(LayoutNode layoutNode) {
        return layoutNode.getY() + layoutNode.getSize().getHeight() / 2;
    }

    protected void initNodePositions() {
        /*
         * need to give nodes initial positions or the animation progress
         * calculation doesn't work (default position is NaN)
         */
        for (LayoutNode layoutNode : graph.getAllNodes()) {
            layoutNode.setPosition(0, 0);
        }
    }

}