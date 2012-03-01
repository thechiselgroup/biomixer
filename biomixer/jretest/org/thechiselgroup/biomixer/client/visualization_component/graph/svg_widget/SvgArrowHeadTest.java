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
package org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget;

import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.svg.AbstractSvgTest;

public class SvgArrowHeadTest extends AbstractSvgTest {

    @Test
    public void createArrowOnFlatLine() {
        SvgArrowHead svgArrowHead = new SvgArrowHead(svgElementFactory,
                new PointDouble(0, 0), new PointDouble(100, 0));
        assertElementEqualsFile("arrowHeadOnFlatLineLeftToRight",
                svgArrowHead.asSvgElement());
    }

    @Test
    public void createArrowRotate45DegreesClockwise() {
        SvgArrowHead svgArrowHead = new SvgArrowHead(svgElementFactory,
                new PointDouble(0, 0), new PointDouble(100, 100));
        assertElementEqualsFile("arrowHeadRotated45degreesClockwise",
                svgArrowHead.asSvgElement());
    }

    @Test
    public void createArrowRotatedThenUpdateSourceLocation() {
        SvgArrowHead svgArrowHead = new SvgArrowHead(svgElementFactory,
                new PointDouble(0, 0), new PointDouble(100, 100));
        svgArrowHead.alignWithPoints(new PointDouble(0, 100), new PointDouble(
                100, 100));
        assertElementEqualsFile("arrowHeadFlatWithYOffset",
                svgArrowHead.asSvgElement());
    }

}
