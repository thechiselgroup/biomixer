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

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.svg.AbstractSvgTest;

public class BoxedTextSvgFactoryTest extends AbstractSvgTest {

    private BoxedTextSvgFactory underTest;

    @Before
    public void setUpBoxedTextFactory() {
        underTest = new BoxedTextSvgFactory(svgElementFactory);
    }

    @Test
    public void shortTextSurroundedByBox() {
        BoxedTextSvgElement boxedText = underTest.createBoxedText("testing");
        assertElementEqualsFile("shortBoxedText", boxedText.getContainer());
    }

}
