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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.circle;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.AbstractLayoutAlgorithmTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.circle.CircleLayoutAlgorithm;

public class CircleLayoutAlgorithmTest extends AbstractLayoutAlgorithmTest {

    @Override
    protected void assertComputationRunningState(LayoutComputation computation) {
        assertFalse(computation.isRunning());
    }

    @Test
    public void fourNodesEqualSize() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(4);

        computeLayout(graph);

        // default nodes have width and height of 10
        assertNodeHasCentre(200, 25, nodes[0]);
        assertNodeHasCentre(375, 200, nodes[1]);
        assertNodeHasCentre(200, 375, nodes[2]);
        assertNodeHasCentre(25, 200, nodes[3]);
    }

    private void setAngleRange(double minAngle, double maxAngle) {
        ((CircleLayoutAlgorithm) underTest).setAngleRange(minAngle, maxAngle);
    }

    @Before
    public void setUp() {
        underTest = new CircleLayoutAlgorithm(errorHandler, nodeAnimator);
    }

    @Test
    public void singleNodeFullCircle() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(1);

        computeLayout(graph);

        assertNodeHasCentre(200, 200, nodes[0]);
    }

    @Test
    public void singleNodeSemicircle() {
        setAngleRange(0, 180);
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(1);

        computeLayout(graph);

        assertNodeHasCentre(200, 200, nodes[0]);
    }

    @Test
    public void threeNodesEqualSizeInLeft180() {
        setAngleRange(180.0, 360.0);
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(3);

        computeLayout(graph);

        assertNodeHasCentre(200, 375, nodes[0]);
        assertNodeHasCentre(25, 200, nodes[1]);
        assertNodeHasCentre(200, 25, nodes[2]);
    }

    @Test
    public void threeNodesEqualSizeInRight180() {
        setAngleRange(0.0, 180.0);
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(3);

        computeLayout(graph);

        assertNodeHasCentre(200, 25, nodes[0]);
        assertNodeHasCentre(375, 200, nodes[1]);
        assertNodeHasCentre(200, 375, nodes[2]);
    }

}
