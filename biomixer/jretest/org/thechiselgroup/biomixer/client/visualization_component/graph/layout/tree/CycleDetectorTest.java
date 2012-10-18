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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.AbstractLayoutGraphTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.CycleDetector;

/**
 * Tests for {@link CycleDetector}.
 * 
 * @author drusk
 * 
 */
public class CycleDetectorTest extends AbstractLayoutGraphTest {

    private void createDefaultGraph() {
        createGraph(0, 0, 400, 400);
    }

    @Test
    public void nineNodesThreeStronglyConnectedComponents() {
        createDefaultGraph();
        LayoutNode[] nodes = createNodes(9);
        createArc(nodes[0], nodes[1]);
        createArc(nodes[1], nodes[2]);
        createArc(nodes[2], nodes[0]);
        createArc(nodes[1], nodes[3]);
        createArc(nodes[2], nodes[3]);
        createArc(nodes[1], nodes[5]);
        createArc(nodes[3], nodes[4]);
        createArc(nodes[4], nodes[3]);
        createArc(nodes[5], nodes[4]);
        createArc(nodes[5], nodes[6]);
        createArc(nodes[6], nodes[5]);
        createArc(nodes[6], nodes[7]);
        createArc(nodes[7], nodes[6]);
        createArc(nodes[6], nodes[8]);
        createArc(nodes[8], nodes[6]);
        createArc(nodes[7], nodes[4]);

        CycleDetector cycleDetector = new CycleDetector(graph);
        List<Set<LayoutNode>> stronglyConnectedComponents = cycleDetector
                .getStronglyConnectedComponents();
        assertThat(stronglyConnectedComponents.size(), equalTo(3));
        assertTrue(cycleDetector.hasCycles());

        for (Set<LayoutNode> stronglyConnectedComponent : stronglyConnectedComponents) {
            if (stronglyConnectedComponent.size() == 4) {
                assertThat(stronglyConnectedComponent,
                        containsExactly(new LayoutNode[] { nodes[5], nodes[6],
                                nodes[7], nodes[8] }));
            } else if (stronglyConnectedComponent.size() == 3) {
                assertThat(stronglyConnectedComponent,
                        containsExactly(new LayoutNode[] { nodes[0], nodes[1],
                                nodes[2] }));
            } else {
                assertThat(
                        stronglyConnectedComponent,
                        containsExactly(new LayoutNode[] { nodes[3], nodes[4] }));
            }
        }
    }

    @Test
    public void threeNodesAcyclic() {
        createDefaultGraph();
        LayoutNode[] nodes = createNodes(3);
        createArc(nodes[0], nodes[1]);
        createArc(nodes[0], nodes[2]);
        createArc(nodes[1], nodes[2]);

        CycleDetector cycleDetector = new CycleDetector(graph);
        List<Set<LayoutNode>> stronglyConnectedComponents = cycleDetector
                .getStronglyConnectedComponents();
        assertThat(stronglyConnectedComponents.size(), equalTo(3));
        assertFalse(cycleDetector.hasCycles());
    }

    @Test
    public void threeNodesInCycleOneNot() {
        createDefaultGraph();
        LayoutNode[] nodes = createNodes(4);
        createArc(nodes[0], nodes[1]);
        createArc(nodes[1], nodes[2]);
        createArc(nodes[2], nodes[0]);
        createArc(nodes[3], nodes[0]);

        CycleDetector cycleDetector = new CycleDetector(graph);
        List<Set<LayoutNode>> stronglyConnectedComponents = cycleDetector
                .getStronglyConnectedComponents();
        assertThat(stronglyConnectedComponents.size(), equalTo(2));
        assertTrue(cycleDetector.hasCycles());

        for (Set<LayoutNode> stronglyConnectedComponent : stronglyConnectedComponents) {
            if (stronglyConnectedComponent.size() == 1) {
                assertThat(stronglyConnectedComponent,
                        containsExactly(new LayoutNode[] { nodes[3] }));
            } else {
                assertThat(stronglyConnectedComponent,
                        containsExactly(new LayoutNode[] { nodes[0], nodes[1],
                                nodes[2] }));
            }
        }

        Set<LayoutNode> stronglyConnectedComponent = stronglyConnectedComponents
                .get(0);
        assertThat(
                stronglyConnectedComponent,
                containsExactly(new LayoutNode[] { nodes[0], nodes[1], nodes[2] }));
    }

}
