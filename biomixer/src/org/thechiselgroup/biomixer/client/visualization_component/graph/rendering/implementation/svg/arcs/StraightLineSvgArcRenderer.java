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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.arcs;

import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.ArcRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

/**
 * Renders an arc as a straight line using SVG.
 * 
 * @author drusk
 * 
 */
public class StraightLineSvgArcRenderer implements ArcRenderer {

    private SvgElementFactory svgElementFactory;

    private TextBoundsEstimator textBoundsEstimator;

    public StraightLineSvgArcRenderer(SvgElementFactory svgElementFactory,
            TextBoundsEstimator textBoundsEstimator) {
        this.svgElementFactory = svgElementFactory;
        this.textBoundsEstimator = textBoundsEstimator;
    }

    @Override
    public RenderedArc createRenderedArc(Arc arc, boolean renderLabel,
            RenderedNode source, RenderedNode target) {

        // NB Refactored so that the rendered svg arc class dealt with the
        // production of associated elements and container.
        // The factory is merely providing some other classes to the arc
        // constructor. This can be StraightLineRenderedSvgArc as it
        // was before if the graph in question cannot handle twice as many
        // rendered lines.
        StraightLineWideMousableRenderedSvgArc renderedArc = new StraightLineWideMousableRenderedSvgArc(
                arc, renderLabel, source, target, svgElementFactory,
                textBoundsEstimator);
        // Ensures consistent setting of endpoints, since this is used later.
        renderedArc.update();
        return renderedArc;
    }

}
