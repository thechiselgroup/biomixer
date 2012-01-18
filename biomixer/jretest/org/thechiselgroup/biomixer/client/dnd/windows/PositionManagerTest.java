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
package org.thechiselgroup.biomixer.client.dnd.windows;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;

public class PositionManagerTest {

    private static final int TEST_HORIZONTAL_STEPS = 5;

    private static final int TEST_PADDING = 5;

    private static final int TEST_VERTICAL_STEPS = 3;

    @Mock
    private SizeInt desktop;

    private PositionManager manager;

    @Test
    public void firstPosition() {
        Point location = manager.getNextLocation(200, 200);

        assertEquals(TEST_PADDING, location.getX());
        assertEquals(TEST_PADDING, location.getY());
    }

    @Test
    public void secondLocation() {
        manager.getNextLocation(200, 200);

        Point location = manager.getNextLocation(200, 200);

        assertEquals(TEST_PADDING
                + ((500 - 200 - 2 * TEST_PADDING) / TEST_HORIZONTAL_STEPS),
                location.getX());
        assertEquals(TEST_PADDING
                + ((400 - 200 - 2 * TEST_PADDING) / TEST_VERTICAL_STEPS),
                location.getY());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        manager = new PositionManager(desktop, TEST_HORIZONTAL_STEPS,
                TEST_VERTICAL_STEPS, TEST_PADDING);

        when(desktop.getWidth()).thenReturn(500);
        when(desktop.getHeight()).thenReturn(400);
    }

}
