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

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.choosel.core.client.util.math.MathUtils;

public class EquidistantBinBoundaryCalculatorTest {

    private static final double DELTA = 0.001d;

    private EquidistantBinBoundaryCalculator underTest;

    @Test
    public void noDataValues() {
        assertArrayEquals(new double[] { 0, 0, 0 },
                underTest.calculateBinBoundaries(MathUtils.createNumberArray(),
                        4), DELTA);
    }

    @Test
    public void range0to10With1Bins() {
        assertArrayEquals(
                new double[] {},
                underTest.calculateBinBoundaries(
                        MathUtils.createNumberArray(0d, 10d), 1), DELTA);
    }

    @Test
    public void range0to10With2Bins() {
        assertArrayEquals(
                new double[] { 5 },
                underTest.calculateBinBoundaries(
                        MathUtils.createNumberArray(0d, 10d), 2), DELTA);
    }

    @Test
    public void range0to10With3Bins() {
        assertArrayEquals(
                new double[] { 3.333, 6.666 },
                underTest.calculateBinBoundaries(
                        MathUtils.createNumberArray(0d, 10d), 3), DELTA);
    }

    @Test
    public void range0to10With4Bins() {
        assertArrayEquals(
                new double[] { 2.5, 5, 7.5 },
                underTest.calculateBinBoundaries(
                        MathUtils.createNumberArray(0d, 10d), 4), DELTA);
    }

    @Test
    public void range0to10With5Bins() {
        assertArrayEquals(
                new double[] { 2, 4, 6, 8 },
                underTest.calculateBinBoundaries(
                        MathUtils.createNumberArray(0d, 10d), 5), DELTA);
    }

    @Test
    public void range0to2With10Bins() {
        assertArrayEquals(
                new double[] { 0.2, 0.4, 0.6, 0.8, 1, 1.2, 1.4, 1.6, 1.8 },
                underTest.calculateBinBoundaries(
                        MathUtils.createNumberArray(0d, 2d), 10), DELTA);
    }

    @Test
    public void rangeMinus10to10With4Bins() {
        assertArrayEquals(
                new double[] { -5, 0, 5 },
                underTest.calculateBinBoundaries(
                        MathUtils.createNumberArray(-10d, 10d), 4), DELTA);
    }

    @Before
    public void setUp() {
        underTest = new EquidistantBinBoundaryCalculator();
    }

    @Test
    public void singleDataValue() {
        assertArrayEquals(
                new double[] { 0 },
                underTest.calculateBinBoundaries(
                        MathUtils.createNumberArray(0d), 2), DELTA);
    }
}