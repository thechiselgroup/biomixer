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
package org.thechiselgroup.biomixer.client.core.visualization.behaviors;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.HighlightingModel;

public class HighlightingManagerTest {

    @Mock
    private HighlightingModel hoverModel;

    @Mock
    private ResourceSet resources;

    private HighlightingManager underTest;

    /**
     * remove highlighting on disposal (issue 65: highlighting remains after
     * window is closed)
     */
    @Test
    public void dispose() {
        underTest.setHighlighting(true);
        underTest.dispose();

        verify(hoverModel, times(1)).removeHighlightedResources(resources);
    }

    @Test
    public void neverRemovedWhenJustSetToFalse() {
        underTest.setHighlighting(false);

        verify(hoverModel, never()).removeHighlightedResources(eq(resources));
    }

    @Test
    public void removedOnlyOnce() {
        underTest.setHighlighting(true);
        underTest.setHighlighting(false);
        underTest.setHighlighting(false);

        verify(hoverModel, times(1)).removeHighlightedResources(resources);
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        underTest = new HighlightingManager(hoverModel, resources);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }
}
