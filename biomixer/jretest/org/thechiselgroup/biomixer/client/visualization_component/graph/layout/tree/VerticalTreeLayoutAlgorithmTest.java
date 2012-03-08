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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.tree;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.AbstractLayoutAlgorithmTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.VerticalTreeLayoutAlgorithm;

public class VerticalTreeLayoutAlgorithmTest extends
        AbstractLayoutAlgorithmTest {

    private VerticalTreeLayoutAlgorithm underTest;

    private void computeLayout(TestLayoutGraph graph) {
        LayoutComputation layoutComputation = underTest.computeLayout(graph);
        assertFalse(layoutComputation.isRunning());
    }

    @Before
    public void setUp() {
        underTest = new VerticalTreeLayoutAlgorithm(errorHandler);
    }

    @Test
    public void singleNode() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(1);

        computeLayout(graph);

        assertNodeHasCentre(200, 200, nodes[0]);
    }

    @Test
    public void threeNodesInTwoTrees() {
        createGraph(0, 0, 400, 400, 1, 1);
        LayoutNode[] nodes = createNodes(0, 3);
        createArc(0, nodes[1], nodes[0]);

        computeLayout(graph);

        /*
         * TODO generic way of specifying this kind of situation (i.e. when the
         * horizontal order of the nodes is not guaranteed, since the input is
         * based on sets)
         */
        if (getCentreX(nodes[0]) == 100) {
            assertNodeHasCentre(100, 400.0 / 3, nodes[0]);
            assertNodeHasCentre(100, 800.0 / 3, nodes[1]);
            assertNodeHasCentre(300, 200, nodes[2]);
        } else {
            assertNodeHasCentre(300, 400.0 / 3, nodes[0]);
            assertNodeHasCentre(300, 800.0 / 3, nodes[1]);
            assertNodeHasCentre(100, 200, nodes[2]);
        }
    }

    @Test
    public void twoNodesConnectedByArc() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(2);

        createArc(nodes[0], nodes[1]);

        computeLayout(graph);
        assertNodesHaveCentreX(200.0, nodes[0], nodes[1]);
        assertNodesHaveCentreY(400.0 / 3, nodes[1]);
        assertNodesHaveCentreY(800.0 / 3, nodes[0]);
    }

    @Test
    public void twoPathsSameTree() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(4);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[2], nodes[0]);
        createArc(nodes[3], nodes[1]);
        createArc(nodes[3], nodes[2]);

        computeLayout(graph);
        assertNodeHasCentre(200, 100, nodes[0]);
        /*
         * TODO generic way of specifying this kind of situation (i.e. when the
         * horizontal order of the nodes is not guaranteed, since the input is
         * based on sets)
         */
        if (getCentreX(nodes[1]) == 400.0 / 3) {
            assertNodeHasCentre(400.0 / 3, 200, nodes[1]);
            assertNodeHasCentre(800.0 / 3, 200, nodes[2]);
        } else {
            assertNodeHasCentre(800.0 / 3, 200, nodes[1]);
            assertNodeHasCentre(400.0 / 3, 200, nodes[2]);
        }
        assertNodeHasCentre(200, 300, nodes[3]);
    }

}
