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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.util.animation.TestAnimationRunner;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutNodeType;

public class LayoutNodeAnimationTest {

    private double delta = 0.1;

    private TestAnimationRunner animationRunner;

    private static int DURATION = 4;

    private void assertNodeLocation(LayoutNode node, double expectedX,
            double expectedY) {
        assertEquals(expectedX, node.getX(), delta);
        assertEquals(expectedY, node.getY(), delta);
    }

    @Test
    public void checkPositionAtHalfway() {
        LayoutNode node = createNodeAt(10, 10);
        runAnimation(node, 30, 30);
        animationRunner.progressTo(0.5);
        assertNodeLocation(node, 20, 20);
    }

    private LayoutNode createNodeAt(double x, double y) {
        TestLayoutNode node = new TestLayoutNode(10, 10, false,
                new TestLayoutNodeType());
        node.setPosition(x, y);
        return node;
    }

    private void runAnimation(LayoutNode node, double destinationX,
            double destinationY) {
        animationRunner.run(new LayoutNodeAnimation(node, destinationX,
                destinationY), DURATION);
    }

    @Before
    public void setUp() {
        animationRunner = new TestAnimationRunner();
    }

}
