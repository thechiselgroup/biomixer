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

import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.animation.TestAnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.text.TestTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

/**
 * This class provides default values for anything that would normally involve
 * GWT dependencies to retrieve, and therefore not be usable in regular unit
 * tests. It also provides methods for firing test events.
 * 
 * @author drusk
 * 
 */
public class TestGraphSvgDisplay extends GraphDisplayController {

    private AnimationRunner animationRunner;

    public TestGraphSvgDisplay(int width, int height,
            SvgElementFactory svgElementFactory) {
        super(width, height, svgElementFactory);
    }

    @Override
    protected AnimationRunner getAnimationRunner() {
        this.animationRunner = new TestAnimationRunner();
        return animationRunner;
    }

    @Override
    public int getGraphAbsoluteLeft() {
        return 0;
    }

    @Override
    public int getGraphAbsoluteTop() {
        return 0;
    }

    public TestAnimationRunner getTestAnimationRunner() {
        return (TestAnimationRunner) animationRunner;
    }

    @Override
    protected TextBoundsEstimator getTextBoundsEstimator() {
        return new TestTextBoundsEstimator(10, 20);
    }

}
