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
package org.thechiselgroup.biomixer.client.dnd.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.dnd.test.DndTestHelpers;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowPanel;
import org.thechiselgroup.choosel.core.client.geometry.Rectangle;
import org.thechiselgroup.choosel.core.client.test.mockito.MockitoGWTBridge;

public class AreaTest {

    @Mock
    private WindowPanel otherWindow1;

    @Mock
    private WindowPanel otherWindow2;

    @Mock
    private WindowPanel window;

    @Test
    public void getVisiblePartsDoesNotReturnSameAreaTwice() {
        Area underTest = new Area(new Rectangle(0, 0, 100, 100), window, null);

        List<Area> windowAreas = new ArrayList<Area>();
        windowAreas.add(new Area(new Rectangle(0, 0, 50, 100), otherWindow1,
                null));
        windowAreas.add(new Area(new Rectangle(0, 0, 50, 100), otherWindow2,
                null));

        when(window.getZIndex()).thenReturn(0);
        when(otherWindow1.getZIndex()).thenReturn(1);
        when(otherWindow2.getZIndex()).thenReturn(2);

        List<Area> result = underTest.getVisibleParts(windowAreas);
        assertEquals(1, result.size());

        Rectangle rectangle = result.get(0).getRectangle();
        assertEquals(50, rectangle.getX());
        assertEquals(50, rectangle.getWidth());
        assertEquals(0, rectangle.getY());
        assertEquals(100, rectangle.getHeight());
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge bridge = MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);
        DndTestHelpers.mockDragClientBundle(bridge);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }
}
