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
import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.SpringAttractionForceCalculator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.Vector2D;

public class SpringAttractionForceCalculatorTest extends
        AbstractForceCalculatorTest {

    private SpringAttractionForceCalculator underTest;

    @Test
    public void distanceVectorPositiveXPositiveY() {
        LayoutNode node1 = createNode(0, 0);
        LayoutNode node2 = createNode(10, 20);
        Vector2D force = underTest.getForce(node1, node2);

        // assertThat(force.getXComponent())
    }

    @Before
    public void setUpUnderTest() {
        underTest = new SpringAttractionForceCalculator(1);
    }

}
