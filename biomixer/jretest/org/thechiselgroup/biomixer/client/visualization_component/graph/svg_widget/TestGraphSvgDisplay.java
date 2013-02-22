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

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.animation.NodeAnimationFactory;
import org.thechiselgroup.biomixer.client.core.util.animation.NullNodeAnimationFactory;
import org.thechiselgroup.biomixer.client.core.util.animation.TestAnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.executor.DelayedExecutor;
import org.thechiselgroup.biomixer.client.core.util.executor.TestDelayedExecutor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.AbstractGraphRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.ArcSizeTransformer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.NodeSizeTransformer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.SvgGraphRenderer;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;

/**
 * This class provides default values for anything that would normally involve
 * GWT dependencies to retrieve, and therefore not be usable in regular unit
 * tests. It also provides methods for firing test events.
 * 
 * @author drusk
 * 
 */
public class TestGraphSvgDisplay extends GraphDisplayController {

    public TestGraphSvgDisplay(int width, int height,
            AbstractGraphRenderer graphRenderer, ErrorHandler errorHandler,
            NodeSizeTransformer nodeSizeTransformer,
            ArcSizeTransformer arcSizeTransformer) {
        super(width, height, "Test Graph View", graphRenderer, errorHandler,
                nodeSizeTransformer, arcSizeTransformer, false);
    }

    public SvgElement asSvg() {
        return ((SvgGraphRenderer) graphRenderer).asSvg();
    }

    @Override
    protected DelayedExecutor getDelayedExecutor() {
        return new TestDelayedExecutor();
    }

    @Override
    public int getGraphAbsoluteLeft() {
        return 0;
    }

    @Override
    public int getGraphAbsoluteTop() {
        return 0;
    }

    @Override
    protected NodeAnimationFactory getNodeAnimationFactory() {
        return new NullNodeAnimationFactory();
    }

    public TestAnimationRunner getTestAnimationRunner() {
        return (TestAnimationRunner) animationRunner;
    }

}
