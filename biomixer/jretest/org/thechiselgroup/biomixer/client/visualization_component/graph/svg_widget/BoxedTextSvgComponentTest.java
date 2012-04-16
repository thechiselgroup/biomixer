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
import org.thechiselgroup.biomixer.client.core.util.text.TestTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.svg.AbstractSvgTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.BoxedTextSvgComponent;

public class BoxedTextSvgComponentTest extends AbstractSvgTest {

    private TestTextBoundsEstimator textBoundsEstimator = new TestTextBoundsEstimator(
            10, 20);

    @Test
    public void longTextMultipleWordsWrapToSecondLine() {
        BoxedTextSvgComponent boxedText = new BoxedTextSvgComponent(
                "testing a very long label", textBoundsEstimator,
                svgElementFactory);
        assertElementEqualsFile("longTextMultipleWordsWrapped",
                boxedText.asSvgElement());
    }

    @Test
    public void shortTextNoWrapping() {
        BoxedTextSvgComponent boxedText = new BoxedTextSvgComponent("testing",
                textBoundsEstimator, svgElementFactory);
        assertElementEqualsFile("shortBoxedText", boxedText.asSvgElement());
    }

}
