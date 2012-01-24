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
package org.thechiselgroup.biomixer.client.core.visualization.model.implementation;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplayCallback;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;

/**
 * <p>
 * Tests the {@link ViewContentDisplayCallback} exposed by
 * {@link DefaultVisualizationModel} to its {@link ViewContentDisplay}.
 * </p>
 * 
 * @author Lars Grammel
 */
public class DefaultVisualizationModelViewContentDisplayCallbackTest {

    private DefaultVisualizationModel underTest;

    private DefaultVisualizationModelTestHelper helper;

    private VisualItemContainer container;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        helper = new DefaultVisualizationModelTestHelper();

        underTest = helper.createTestVisualizationModel();

        ArgumentCaptor<ViewContentDisplayCallback> callbackCaptor = ArgumentCaptor
                .forClass(ViewContentDisplayCallback.class);
        ArgumentCaptor<VisualItemContainer> containerCaptor = ArgumentCaptor
                .forClass(VisualItemContainer.class);
        verify(helper.getViewContentDisplay(), times(1)).init(
                containerCaptor.capture(), callbackCaptor.capture());
        container = containerCaptor.getValue();
    }

    @Test
    public void viewContentDisplayIsInitialized() {
        verify(helper.getViewContentDisplay(), times(1)).init(
                any(VisualItemContainer.class),
                any(ViewContentDisplayCallback.class));
    }

    @Test
    public void visualItemContainerIsErrorFreee() {
        assertThat(container,
                sameInstance(underTest.getErrorFreeVisualItemContainer()));
    }
}