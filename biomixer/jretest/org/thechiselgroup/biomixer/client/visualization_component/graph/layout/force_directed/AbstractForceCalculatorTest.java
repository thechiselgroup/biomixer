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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.force_directed;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNodeType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutNode;

/**
 * Contains setup for force calculator tests.
 * 
 * @author drusk
 * 
 */
public class AbstractForceCalculatorTest {

    @Mock
    private LayoutNodeType nodeType;

    protected LayoutNode createNode(double x, double y) {
        TestLayoutNode node = new TestLayoutNode(10, 10, false, nodeType);
        node.setPosition(x, y);
        return node;
    }

    @Before
    public void setUpMocks() {
        MockitoAnnotations.initMocks(this);
    }

}
