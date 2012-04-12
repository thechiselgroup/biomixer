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
package org.thechiselgroup.biomixer.client.core.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PointUtilsTest {

    private static double delta = 0.1;

    @Test
    public void getRotationAngleFlatLineLeftToRight() {
        assertEquals(PointUtils.getRotationAngle(new PointDouble(0, 0),
                new PointDouble(100, 0)), 0.0, delta);
    }

    @Test
    public void getRotationAngleFlatLineRightToLeft() {
        assertEquals(PointUtils.getRotationAngle(new PointDouble(100, 0),
                new PointDouble(0, 0)), 180.0, delta);
    }

    @Test
    public void getRotationAngleLowerLeftQuadrant() {
        assertEquals(PointUtils.getRotationAngle(new PointDouble(100, 100),
                new PointDouble(0, 0)), -135.0, delta);
    }

    @Test
    public void getRotationAngleLowerRightQuadrant() {
        assertEquals(PointUtils.getRotationAngle(new PointDouble(0, 100),
                new PointDouble(100, 0)), -45.0, delta);
    }

    @Test
    public void getRotationAngleUpperLeftQuadrant() {
        assertEquals(PointUtils.getRotationAngle(new PointDouble(100, 0),
                new PointDouble(0, 100)), 135.0, delta);
    }

    @Test
    public void getRotationAngleUpperRightQuadrant() {
        assertEquals(PointUtils.getRotationAngle(new PointDouble(0, 0),
                new PointDouble(100, 100)), 45.0, delta);
    }

}
