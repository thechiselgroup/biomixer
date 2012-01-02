/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.choosel.core.client.geometry.Point;
import org.thechiselgroup.choosel.core.client.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.choosel.core.client.util.collections.CollectionUtils;

public class GraphLayoutCommandTest {

    private GraphLayoutCommand underTest;

    @Mock
    private GraphDisplay graphDisplay;

    @Mock
    private Node node;

    @Mock
    private Node node2;

    @Mock
    private Point location;

    @Mock
    private Point location2;

    private String layout = "graph-layout";

    @Test
    public void setGraphLayoutOnExecute() {
        underTest.execute();

        verify(graphDisplay, times(1)).runLayout(eq(layout));
    }

    @Test
    public void setLocationCalledOnUndo() {
        when(graphDisplay.getLocation(node)).thenReturn(location);
        when(graphDisplay.getLocation(node2)).thenReturn(location2);
        underTest.execute();
        underTest.undo();
        verify(graphDisplay, times(1)).setLocation(eq(node), eq(location));
        verify(graphDisplay, times(1)).setLocation(eq(node2), eq(location2));
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        when(node.getId()).thenReturn("1");
        when(node2.getId()).thenReturn("2");

        underTest = new GraphLayoutCommand(graphDisplay, layout,
                CollectionUtils.toList(node, node2));
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }
}
