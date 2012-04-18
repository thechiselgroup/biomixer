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

import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.AbstractLayoutAlgorithmTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.VerticalTreeLayoutAlgorithm;

public class VerticalTreeLayoutAlgorithmTest extends
        AbstractLayoutAlgorithmTest {

    @Override
    protected void assertComputationRunningState(LayoutComputation computation) {
        assertFalse(computation.isRunning());
    }

    @Test
    public void multiRootMultiPathMismatchedLengthsSingleDag() {
        setTreeDirectionUp(true);
        createGraph(0, 0, 400, 400);
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(8);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[2], nodes[0]);
        createArc(nodes[3], nodes[1]);
        createArc(nodes[3], nodes[2]);
        createArc(nodes[4], nodes[3]);
        createArc(nodes[5], nodes[3]);
        createArc(nodes[5], nodes[6]);
        createArc(nodes[7], nodes[4]);
        createArc(nodes[7], nodes[5]);

        computeLayout(graph);
        /*
         * TODO generic way of specifying this kind of situation (i.e. when the
         * horizontal order of the nodes is not guaranteed, since the input is
         * based on sets)
         */
        if (getCentreX(nodes[0]) == 400.0 / 3) {
            assertNodeHasCentre(400.0 / 3, 200.0 / 3, nodes[0]);
            assertNodeHasCentre(800.0 / 3, 200.0 / 3, nodes[6]);
        } else {
            assertNodeHasCentre(400.0 / 3, 200.0 / 3, nodes[6]);
            assertNodeHasCentre(800.0 / 3, 200.0 / 3, nodes[0]);
        }
        if (getCentreX(nodes[1]) == 400.0 / 3) {
            assertNodeHasCentre(400.0 / 3, 400.0 / 3, nodes[1]);
            assertNodeHasCentre(800.0 / 3, 400.0 / 3, nodes[2]);
        } else {
            assertNodeHasCentre(400.0 / 3, 400.0 / 3, nodes[2]);
            assertNodeHasCentre(800.0 / 3, 400.0 / 3, nodes[1]);
        }
        assertNodeHasCentre(200.0, 600.0 / 3, nodes[3]);
        if (getCentreX(nodes[4]) == 400.0 / 3) {
            assertNodeHasCentre(400.0 / 3, 800.0 / 3, nodes[4]);
            assertNodeHasCentre(800.0 / 3, 800.0 / 3, nodes[5]);
        } else {
            assertNodeHasCentre(400.0 / 3, 800.0 / 3, nodes[5]);
            assertNodeHasCentre(800.0 / 3, 800.0 / 3, nodes[4]);
        }
        assertNodeHasCentre(200.0, 1000.0 / 3, nodes[7]);
    }

    @Test
    public void multiRootMultiPathSingleDag() {
        setTreeDirectionUp(true);
        createGraph(0, 0, 400, 400);
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(9);
        createArc(nodes[1], nodes[0]);
        createArc(nodes[3], nodes[0]);
        createArc(nodes[2], nodes[1]);
        createArc(nodes[4], nodes[3]);
        createArc(nodes[4], nodes[2]);
        createArc(nodes[5], nodes[4]);
        createArc(nodes[6], nodes[4]);
        createArc(nodes[8], nodes[5]);
        createArc(nodes[8], nodes[6]);
        createArc(nodes[6], nodes[7]);

        computeLayout(graph);
        /*
         * TODO generic way of specifying this kind of situation (i.e. when the
         * horizontal order of the nodes is not guaranteed, since the input is
         * based on sets)
         */
        if (getCentreX(nodes[0]) == 400.0 / 3) {
            assertNodeHasCentre(400.0 / 3, 400.0 / 7, nodes[0]);
            assertNodeHasCentre(800.0 / 3, 400.0 / 7, nodes[7]);
        } else {
            assertNodeHasCentre(400.0 / 3, 400.0 / 7, nodes[7]);
            assertNodeHasCentre(800.0 / 3, 400.0 / 7, nodes[0]);
        }
        if (getCentreX(nodes[1]) == 400.0 / 3) {
            assertNodeHasCentre(400.0 / 3, 800.0 / 7, nodes[1]);
            assertNodeHasCentre(800.0 / 3, 800.0 / 7, nodes[3]);
        } else {
            assertNodeHasCentre(400.0 / 3, 800.0 / 7, nodes[3]);
            assertNodeHasCentre(800.0 / 3, 800.0 / 7, nodes[1]);
        }
        assertNodeHasCentre(200.0, 1200.0 / 7, nodes[2]);
        assertNodeHasCentre(200.0, 1600.0 / 7, nodes[4]);
        if (getCentreX(nodes[5]) == 400.0 / 3) {
            assertNodeHasCentre(400.0 / 3, 2000.0 / 7, nodes[5]);
            assertNodeHasCentre(800.0 / 3, 2000.0 / 7, nodes[6]);
        } else {
            assertNodeHasCentre(400.0 / 3, 2000.0 / 7, nodes[6]);
            assertNodeHasCentre(800.0 / 3, 2000.0 / 7, nodes[5]);
        }
        assertNodeHasCentre(200.0, 2400.0 / 7, nodes[8]);
    }

    private void setTreeDirectionUp(boolean up) {
        underTest = new VerticalTreeLayoutAlgorithm(up, errorHandler,
                animationRunner);
    }

    @Test
    public void singleNode() {
        setTreeDirectionUp(true);
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(1);

        computeLayout(graph);

        assertNodeHasCentre(200, 200, nodes[0]);
    }

    @Test
    public void threeNodesInTwoTrees() {
        setTreeDirectionUp(true);
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
        setTreeDirectionUp(true);
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(2);

        createArc(nodes[0], nodes[1]);

        computeLayout(graph);
        assertNodesHaveCentreX(200.0, nodes[0], nodes[1]);
        assertNodesHaveCentreY(400.0 / 3, nodes[1]);
        assertNodesHaveCentreY(800.0 / 3, nodes[0]);
    }

    @Test
    public void twoNodesPointingDown() {
        setTreeDirectionUp(false);
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(2);
        createArc(nodes[0], nodes[1]);

        computeLayout(graph);
        assertNodeHasCentre(200, 400.0 / 3, nodes[0]);
        assertNodeHasCentre(200, 800.0 / 3, nodes[1]);
    }

    @Test
    public void twoPathsSameTree() {
        setTreeDirectionUp(true);
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
