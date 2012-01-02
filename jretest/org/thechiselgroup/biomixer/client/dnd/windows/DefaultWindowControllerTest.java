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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.geometry.Point;

// TODO test cases that test minimum sizes
public class DefaultWindowControllerTest {

    private static final int Y = 1;

    private static final int X = 1;

    private DefaultWindowController underTest;

    @Mock
    private WindowCallback callback;

    @Test
    public void preventMoveIfNotResizingVertically() {
        when(callback.getHeight()).thenReturn(100);

        underTest.resize(0, 100, 200, 0);

        verify(callback, times(1)).setPixelSize(200, 0);
        verify(callback, times(1)).setLocation(X + 0, Y + 0);
    }

    @Test
    public void restrictMoveIfPartialVerticallyResize() {
        when(callback.getHeight()).thenReturn(50);

        underTest.resize(0, 100, 200, 0);

        verify(callback, times(1)).setPixelSize(200, 0);
        verify(callback, times(1)).setLocation(X + 0, Y + 50);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = new DefaultWindowController(callback);

        when(callback.getLocation()).thenReturn(new Point(X, Y));
    }

}
