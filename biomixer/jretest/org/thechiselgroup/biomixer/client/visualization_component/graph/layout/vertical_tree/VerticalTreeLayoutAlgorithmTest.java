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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.vertical_tree;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.VerticalTreeLayoutAlgorithm;

public class VerticalTreeLayoutAlgorithmTest extends
        AbstractLayoutAlgorithmTest {

    private VerticalTreeLayoutAlgorithm underTest;

    @Before
    public void setUp() {
        underTest = new VerticalTreeLayoutAlgorithm(errorHandler);
    }

    @Test
    public void singleNode() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node = createDefaultNode(graph);

        LayoutComputation layoutComputation = underTest.computeLayout(graph);

        assertFalse(layoutComputation.isRunning());
        assertNodeHasCentre(200, 200, node);
    }

    @Test
    public void threeNodesInTwoTrees() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);
        LayoutNode node2 = createDefaultNode(graph);
        LayoutNode node3 = createDefaultNode(graph);
        createDefaultArc(graph, node2, node1);

        LayoutComputation layoutComputation = underTest.computeLayout(graph);

        assertFalse(layoutComputation.isRunning());

        // TODO generic way of specifying this kind of situation
        if (getCentreX(node1) == 100) {
            assertNodeHasCentre(100, 400.0 / 3, node1);
            assertNodeHasCentre(100, 800.0 / 3, node2);
            assertNodeHasCentre(300, 200, node3);
        } else {
            assertNodeHasCentre(300, 400.0 / 3, node1);
            assertNodeHasCentre(300, 800.0 / 3, node2);
            assertNodeHasCentre(100, 200, node3);
        }
    }

    @Test
    public void twoNodesConnectedByArc() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode sourceNode = createDefaultNode(graph);
        LayoutNode targetNode = createDefaultNode(graph);
        createDefaultArc(graph, sourceNode, targetNode);

        LayoutComputation layoutComputation = underTest.computeLayout(graph);

        assertFalse(layoutComputation.isRunning());
        assertNodesHaveCentreX(200.0, sourceNode, targetNode);
        assertNodesHaveCentreY(400.0 / 3, targetNode);
        assertNodesHaveCentreY(800.0 / 3, sourceNode);
    }

    @Test
    public void twoPathsSameTree() {
        TestLayoutGraph graph = createGraph(0, 0, 400, 400);
        LayoutNode node1 = createDefaultNode(graph);
        LayoutNode node2 = createDefaultNode(graph);
        LayoutNode node3 = createDefaultNode(graph);
        LayoutNode node4 = createDefaultNode(graph);
        createDefaultArc(graph, node2, node1);
        createDefaultArc(graph, node3, node1);
        createDefaultArc(graph, node4, node2);
        createDefaultArc(graph, node4, node3);

        LayoutComputation layoutComputation = underTest.computeLayout(graph);

        assertFalse(layoutComputation.isRunning());
        assertNodeHasCentre(200, 100, node1);
        // TODO generic way of specifying this kind of situation
        if (getCentreX(node2) == 400.0 / 3) {
            assertNodeHasCentre(400.0 / 3, 200, node2);
            assertNodeHasCentre(800.0 / 3, 200, node3);
        } else {
            assertNodeHasCentre(800.0 / 3, 200, node2);
            assertNodeHasCentre(400.0 / 3, 200, node3);
        }
        assertNodeHasCentre(200, 300, node4);
    }

}
