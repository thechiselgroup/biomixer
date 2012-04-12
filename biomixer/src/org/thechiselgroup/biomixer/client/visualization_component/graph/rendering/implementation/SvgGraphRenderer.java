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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation;

import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.SvgGraphBackground;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

/**
 * Manages low level details of the SVG graph rendering.
 * 
 * @author drusk
 * 
 */
public class SvgGraphRenderer extends AbstractGraphRenderer {

    protected CompositeSvgComponent rootSvgComponent = null;

    private CompositeSvgComponent arcGroup;

    private CompositeSvgComponent nodeGroup;

    protected CompositeSvgComponent popupGroup;

    private SvgGraphBackground background;

    private final SvgElementFactory svgElementFactory;

    public SvgGraphRenderer(int width, int height,
            SvgElementFactory svgElementFactory,
            TextBoundsEstimator textBoundsEstimator) {
        super(new BoxedTextSvgNodeRenderer(svgElementFactory,
                textBoundsEstimator), new SvgArcRenderer(svgElementFactory));
        this.svgElementFactory = svgElementFactory;
        initRootSvgElement();
        initBackground(width, height);
        initSvgGroupings();
    }

    @Override
    protected void addArcToGraph(RenderedArc arc) {
        // FIXME
        arcGroup.appendChild((ArcSvgComponent) arc);
    }

    @Override
    protected void addNodeToGraph(RenderedNode node) {
        // FIXME
        nodeGroup.appendChild((NodeSvgComponent) node);
    }

    /**
     * For testing.
     * 
     * @return
     */
    public SvgElement asSvg() {
        return rootSvgComponent.getSvgElement();
    }

    @Override
    public void bringToForeground(RenderedNode node) {
        // FIXME
        nodeGroup.appendChild((NodeSvgComponent) node);
    }

    /**
     * Clear the node expander popup if there is one. This does not get rid of
     * the on mouse-over node details though, which is done using HTML not SVG.
     */
    @Override
    public void clearPopups() {
        popupGroup.removeAllChildren();
    }

    private CompositeSvgComponent createCompositeGroupingComponent(String id) {
        SvgElement groupingElement = svgElementFactory.createElement(Svg.G);
        groupingElement.setAttribute(Svg.ID, id);
        return new CompositeSvgComponent(groupingElement);
    }

    private void initBackground(int width, int height) {
        background = new SvgGraphBackground(width, height, svgElementFactory);
        rootSvgComponent.appendChild(background.asSvg());
    }

    private void initRootSvgElement() {
        SvgElement root = svgElementFactory.createElement(Svg.SVG);
        root.setAttribute("xmlns", Svg.NAMESPACE);
        root.setAttribute("version", "1.1");
        rootSvgComponent = new CompositeSvgComponent(root);
    }

    private void initSvgGroupings() {
        arcGroup = createCompositeGroupingComponent("arcGroup");
        nodeGroup = createCompositeGroupingComponent("nodeGroup");
        popupGroup = createCompositeGroupingComponent("popupGroup");
        // order is important here - want arcs behind nodes and popups
        rootSvgComponent.appendChild(arcGroup);
        rootSvgComponent.appendChild(nodeGroup);
        rootSvgComponent.appendChild(popupGroup);
    }

    @Override
    protected void removeArcFromGraph(RenderedArc arc) {
        // FIXME
        arcGroup.removeChild((ArcSvgComponent) arc);
    }

    @Override
    protected void removeNodeFromGraph(RenderedNode node) {
        // FIXME
        nodeGroup.removeChild((NodeSvgComponent) node);
    }

    @Override
    public void setViewWideInteractionHandler(ChooselEventHandler handler) {
        rootSvgComponent.setEventListener(handler);
    }

}
