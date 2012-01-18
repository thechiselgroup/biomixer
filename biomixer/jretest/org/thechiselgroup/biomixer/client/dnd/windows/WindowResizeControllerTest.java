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

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

// TODO test cases that test minimum sizes
public class WindowResizeControllerTest {

    private static class TestResizeablePanel implements WindowController {

        private int height;

        private int width;

        public TestResizeablePanel(int width, int height) {
            this.height = height;
            this.width = width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public void resize(int horizontalMove, int verticalMove,
                int targetWidth, int targetHeight) {

            this.width = targetWidth;
            this.height = targetHeight;
        }

    }

    private WindowController panel;

    @Test
    public void eastLeft() {
        // TODO change draggable relative information to window-relative??
        WindowResizeController.resize(690, 600, 700, 600, ResizeDirection.EAST,
                panel);

        verify(panel, times(1)).resize(0, 0, 190, 100);
    }

    @Test
    public void eastRight() {
        // TODO change draggable relative information to window-relative??
        WindowResizeController.resize(710, 600, 700, 600, ResizeDirection.EAST,
                panel);

        verify(panel, times(1)).resize(0, 0, 210, 100);
    }

    @Test
    public void northDown() {
        WindowResizeController.resize(500, 610, 500, 600,
                ResizeDirection.NORTH, panel);

        verify(panel, times(1)).resize(0, 10, 200, 90);
    }

    @Test
    public void northEastDownLeft() {
        WindowResizeController.resize(690, 610, 700, 600,
                ResizeDirection.NORTH_EAST, panel);

        verify(panel, times(1)).resize(0, 10, 190, 90);
    }

    @Test
    public void northEastDownRight() {
        WindowResizeController.resize(710, 610, 700, 600,
                ResizeDirection.NORTH_EAST, panel);

        verify(panel, times(1)).resize(0, 10, 210, 90);
    }

    @Test
    public void northEastUpLeft() {
        WindowResizeController.resize(690, 590, 700, 600,
                ResizeDirection.NORTH_EAST, panel);

        verify(panel, times(1)).resize(0, -10, 190, 110);
    }

    @Test
    public void northEastUpRight() {
        WindowResizeController.resize(710, 590, 700, 600,
                ResizeDirection.NORTH_EAST, panel);

        verify(panel, times(1)).resize(0, -10, 210, 110);
    }

    @Test
    public void northUp() {
        WindowResizeController.resize(500, 590, 500, 600,
                ResizeDirection.NORTH, panel);

        verify(panel, times(1)).resize(0, -10, 200, 110);
    }

    @Test
    public void northWestDownLeft() {
        WindowResizeController.resize(490, 610, 500, 600,
                ResizeDirection.NORTH_WEST, panel);

        verify(panel, times(1)).resize(-10, 10, 210, 90);
    }

    @Test
    public void northWestDownRight() {
        WindowResizeController.resize(510, 610, 500, 600,
                ResizeDirection.NORTH_WEST, panel);

        verify(panel, times(1)).resize(10, 10, 190, 90);
    }

    @Test
    public void northWestUpLeft() {
        WindowResizeController.resize(490, 590, 500, 600,
                ResizeDirection.NORTH_WEST, panel);

        verify(panel, times(1)).resize(-10, -10, 210, 110);
    }

    @Test
    public void northWestUpRight() {
        WindowResizeController.resize(510, 590, 500, 600,
                ResizeDirection.NORTH_WEST, panel);

        verify(panel, times(1)).resize(10, -10, 190, 110);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        panel = spy(new TestResizeablePanel(200, 100));
    }

    @Test
    public void southDown() {
        // TODO change draggable relative information to window-relative??
        WindowResizeController.resize(500, 710, 500, 700,
                ResizeDirection.SOUTH, panel);

        verify(panel, times(1)).resize(0, 0, 200, 110);
    }

    @Test
    public void southEastDownLeft() {
        WindowResizeController.resize(690, 710, 700, 700,
                ResizeDirection.SOUTH_EAST, panel);

        verify(panel, times(1)).resize(0, 0, 190, 110);
    }

    @Test
    public void southEastDownRight() {
        WindowResizeController.resize(710, 710, 700, 700,
                ResizeDirection.SOUTH_EAST, panel);

        verify(panel, times(1)).resize(0, 0, 210, 110);
    }

    @Test
    public void southEastUpLeft() {
        WindowResizeController.resize(690, 690, 700, 700,
                ResizeDirection.SOUTH_EAST, panel);

        verify(panel, times(1)).resize(0, 0, 190, 90);
    }

    @Test
    public void southEastUpRight() {
        WindowResizeController.resize(710, 690, 700, 700,
                ResizeDirection.SOUTH_EAST, panel);

        verify(panel, times(1)).resize(0, 0, 210, 90);
    }

    @Test
    public void southUp() {
        // TODO change draggable relative information to window-relative??
        WindowResizeController.resize(500, 690, 500, 700,
                ResizeDirection.SOUTH, panel);

        verify(panel, times(1)).resize(0, 0, 200, 90);
    }

    @Test
    public void southWestDownLeft() {
        WindowResizeController.resize(490, 710, 500, 700,
                ResizeDirection.SOUTH_WEST, panel);

        verify(panel, times(1)).resize(-10, 0, 210, 110);
    }

    @Test
    public void southWestDownRight() {
        WindowResizeController.resize(510, 710, 500, 700,
                ResizeDirection.SOUTH_WEST, panel);

        verify(panel, times(1)).resize(10, 0, 190, 110);
    }

    @Test
    public void southWestUpLeft() {
        WindowResizeController.resize(490, 690, 500, 700,
                ResizeDirection.SOUTH_WEST, panel);

        verify(panel, times(1)).resize(-10, 0, 210, 90);
    }

    @Test
    public void southWestUpRight() {
        WindowResizeController.resize(510, 690, 500, 700,
                ResizeDirection.SOUTH_WEST, panel);

        verify(panel, times(1)).resize(10, 0, 190, 90);
    }

    @Test
    public void westLeft() {
        WindowResizeController.resize(490, 600, 500, 600, ResizeDirection.WEST,
                panel);

        verify(panel, times(1)).resize(-10, 0, 210, 100);
    }

    @Test
    public void westRight() {
        WindowResizeController.resize(510, 600, 500, 600, ResizeDirection.WEST,
                panel);

        verify(panel, times(1)).resize(10, 0, 190, 100);
    }
}
