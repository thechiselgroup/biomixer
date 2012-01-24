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
package org.thechiselgroup.biomixer.client.core.geometry;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class RectangleTest {

    private Rectangle rectangle;

    @Test
    public void removeEmptyRectangleSet() {
        List<Rectangle> result = rectangle
                .calculateRemainder(new ArrayList<Rectangle>());

        assertEquals(1, result.size());
        assertEquals(rectangle, result.get(0));
    }

    @Before
    public void setUp() {
        this.rectangle = new Rectangle(10, 20, 30, 40);
    }

}
