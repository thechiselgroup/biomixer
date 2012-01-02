/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.visualization.behaviors;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.visualization.behaviors.HighlightingVisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction.Type;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.HighlightingModel;

public class HighlightingVisualItemBehaviorTest {

    private static final String VIEW_ITEM_ID = "visualItemCategory";

    private HighlightingModel hoverModel;

    @Mock
    private VisualItem visualItem;

    private HighlightingVisualItemBehavior underTest;

    private ResourceSet resources;

    /**
     * remove highlighting on disposal (issue 65: highlighting remains after
     * window is closed)
     */
    @Test
    public void disposeRemovesHighlighting() {
        underTest.onVisualItemCreated(visualItem);
        underTest.onInteraction(visualItem, new VisualItemInteraction(
                Type.MOUSE_OVER));
        underTest.onVisualItemRemoved(visualItem);

        assertThat(hoverModel.getResources(),
                containsExactly(ResourceSetTestUtils.createResources()));
    }

    /**
     * Remove highlighting on drag end.
     */
    @Test
    public void dragEndRemovesHighlighting() {
        underTest.onVisualItemCreated(visualItem);
        underTest.onInteraction(visualItem, new VisualItemInteraction(
                Type.MOUSE_OVER));
        underTest.onInteraction(visualItem, new VisualItemInteraction(
                Type.DRAG_START));
        underTest.onInteraction(visualItem,
                new VisualItemInteraction(Type.DRAG_END));

        assertThat(hoverModel.getResources(),
                containsExactly(ResourceSetTestUtils.createResources()));
    }

    @Test
    public void mouseOverAddsResourcesToHoverModel() {
        underTest.onVisualItemCreated(visualItem);
        underTest.onInteraction(visualItem, new VisualItemInteraction(
                Type.MOUSE_OVER));
        assertThat(hoverModel.getResources(),
                containsExactly(ResourceSetTestUtils.createResources(1, 2)));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        hoverModel = spy(new HighlightingModel());

        resources = ResourceSetTestUtils.createResources(1, 2);
        when(visualItem.getId()).thenReturn(VIEW_ITEM_ID);
        when(visualItem.getResources()).thenReturn(resources);

        underTest = new HighlightingVisualItemBehavior(hoverModel);
    }
}