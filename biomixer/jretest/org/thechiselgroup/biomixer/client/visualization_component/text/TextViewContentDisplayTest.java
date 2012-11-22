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
package org.thechiselgroup.biomixer.client.visualization_component.text;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.client.core.util.collections.Delta.createAddedDelta;
import static org.thechiselgroup.biomixer.client.core.util.collections.Delta.createUpdatedDelta;
import static org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections.toCollection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.DataTypeValidator;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplayCallback;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Status;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Subset;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemTestUtils;

public class TextViewContentDisplayTest {

    @Mock
    private ViewContentDisplayCallback callback;

    private TextVisualization underTest;

    @Mock
    private TextItemContainer textItemContainer;

    @Mock
    private TextItemLabel itemLabel;

    @Mock
    private ResourceCategorizer resourceCategorizer;

    @Mock
    private VisualItemContainer visualItemContainer;

    @Mock
    private DataTypeValidator dataTypeValidator;

    @Test
    public void partialSelectionShownCorrectly_Issue73() {
        // create resource item that contains 2 resources
        VisualItem visualItem = VisualItemTestUtils.createVisualItem("",
                ResourceSetTestUtils.createResources(1, 2));

        when(visualItem.getValue(TextVisualization.FONT_SIZE_SLOT)).thenReturn(
                new Double(2));

        when(visualItem.getStatus(Subset.HIGHLIGHTED)).thenReturn(Status.NONE);
        when(visualItem.getStatus(Subset.SELECTED)).thenReturn(Status.NONE);

        underTest.update(createAddedDelta(toCollection(visualItem)),
                LightweightCollections.<Slot> emptySet());

        // both resources get highlighted as the selection is dragged
        when(visualItem.getStatus(Subset.HIGHLIGHTED)).thenReturn(Status.FULL);
        underTest.update(createUpdatedDelta(toCollection(visualItem)),
                LightweightCollections.<Slot> emptyCollection());

        // create selection that contains one of those resources
        when(visualItem.getStatus(Subset.SELECTED)).thenReturn(Status.PARTIAL);
        underTest.update(createUpdatedDelta(toCollection(visualItem)),
                LightweightCollections.<Slot> emptySet());

        reset(itemLabel);

        // highlighting is removed after drag operation
        when(visualItem.getStatus(Subset.HIGHLIGHTED)).thenReturn(Status.NONE);
        underTest.update(createUpdatedDelta(LightweightCollections
                .toCollection(visualItem)), LightweightCollections
                .<Slot> emptySet());

        // check label status (should be: partially selected, but not partially
        // highlighted)
        verify(itemLabel, times(1)).addStyleName(TextItem.CSS_SELECTED);
        verify(itemLabel, times(1)).removeStyleName(
                TextItem.CSS_PARTIALLY_HIGHLIGHTED);
        verify(itemLabel, times(1)).removeStyleName(TextItem.CSS_HIGHLIGHTED);
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        when(textItemContainer.createTextItemLabel(any(VisualItem.class)))
                .thenReturn(itemLabel);

        underTest = new TextVisualization(textItemContainer, dataTypeValidator);

        underTest.init(visualItemContainer, callback);

        when(resourceCategorizer.getCategory(any(Resource.class))).thenReturn(
                ResourceSetTestUtils.TYPE_1);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }

}
