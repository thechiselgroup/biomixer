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

import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers;

public class GraphLayoutTest extends AbstractLayoutGraphTest {

    @Test
    public void allArcsDirectedAwayFromNode() {
        LayoutNode[] nodes = createNodes(3);
        LayoutArc arc1 = createArc(nodes[0], nodes[1]);
        LayoutArc arc2 = createArc(nodes[0], nodes[2]);

        List<LayoutArc> connectedArcs = nodes[0].getConnectedArcs();
        assertThat(connectedArcs,
                CollectionMatchers.containsExactly(arc1, arc2));
    }

    @Test
    public void arcsDirectedToAndFromNode() {
        LayoutNode[] nodes = createNodes(3);
        LayoutArc arc1 = createArc(nodes[1], nodes[0]);
        LayoutArc arc2 = createArc(nodes[0], nodes[2]);

        List<LayoutArc> connectedArcs = nodes[0].getConnectedArcs();
        assertThat(connectedArcs,
                CollectionMatchers.containsExactly(arc1, arc2));
    }

    @Test
    public void extraArcsNotConnected() {
        LayoutNode[] nodes = createNodes(5);
        LayoutArc arc1 = createArc(nodes[1], nodes[0]);
        LayoutArc arc2 = createArc(nodes[0], nodes[2]);
        createArc(nodes[3], nodes[4]);

        List<LayoutArc> connectedArcs = nodes[0].getConnectedArcs();
        assertThat(connectedArcs,
                CollectionMatchers.containsExactly(arc1, arc2));
    }

    @Before
    public void setUp() {
        createGraph(0, 0, 100, 100);
    }
}
