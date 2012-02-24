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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeInt;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;

public class VerticalTreeLayoutTest {

    private VerticalTreeLayout underTest;

    private GraphLayoutCallback callback;

    private GraphLayoutCallback initializeGraphLayoutCallback(int width,
            int height) {
        GraphLayoutCallback callback = mock(Graph.class);
        when(callback.getDisplayArea()).thenReturn(
                new DefaultSizeInt(width, height));

        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                NodeItem node = (NodeItem) args[0];
                Point point = (Point) args[1];
                GraphLayoutCallback mock = (GraphLayoutCallback) invocation
                        .getMock();
                when(mock.getLocation(node)).thenReturn(point);
                return null;
            }

        }).when(callback).setLocation(any(NodeItem.class), any(Point.class));

        return callback;
    }

    public void run(StubGraphStructure stubGraph) {
        underTest.run(stubGraph.getNodeItems().toArray(new NodeItem[0]),
                stubGraph.getArcItems().toArray(new ArcItem[0]), callback);
    }

    @Before
    public void setUp() {
        underTest = new VerticalTreeLayout();
        callback = initializeGraphLayoutCallback(100, 100);
    }

    @Test
    public void singleNode() {
        StubGraphStructure stubGraph = new StubGraphStructure(1);
        run(stubGraph);
        assertThat(callback.getLocation(stubGraph.getNodeItem(0)),
                equalTo(new Point(50, 50)));
    }

    @Test
    public void threeNodesTwoTrees() {
        StubGraphStructure stubGraph = new StubGraphStructure(3);
        stubGraph.createArc(0, 1);
        run(stubGraph);
        // XXX what if the trees were returned in the opposite order. The test
        // would break.
        // TODO make more flexible using CollectionMatchers
        assertThat(callback.getLocation(stubGraph.getNodeItem(0)),
                equalTo(new Point(75, 33)));
        assertThat(callback.getLocation(stubGraph.getNodeItem(1)),
                equalTo(new Point(75, 66)));
        assertThat(callback.getLocation(stubGraph.getNodeItem(2)),
                equalTo(new Point(25, 50)));
    }

    @Test
    public void twoNodesSameTree() {
        StubGraphStructure stubGraph = new StubGraphStructure(2);
        stubGraph.createArc(0, 1);
        run(stubGraph);
        assertThat(callback.getLocation(stubGraph.getNodeItem(0)),
                equalTo(new Point(50, 33)));
        assertThat(callback.getLocation(stubGraph.getNodeItem(1)),
                equalTo(new Point(50, 66)));
    }

    @Ignore("TODO: test tree with two paths")
    @Test
    public void twoPathsSameTree() {
        StubGraphStructure stubGraph = new StubGraphStructure(5);
        stubGraph.createArc(0, 1);
        stubGraph.createArc(1, 2);
        stubGraph.createArc(2, 4);
        stubGraph.createArc(0, 3);
        stubGraph.createArc(3, 4);
        run(stubGraph);
        // assertThat(callback.getLocation(stubGraph.getNodeItem(0)))
    }
}
