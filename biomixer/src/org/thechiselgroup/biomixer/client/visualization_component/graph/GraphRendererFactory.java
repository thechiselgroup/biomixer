package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.ArcRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeExpanderRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.NodeSizeTransformer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.SvgGraphRenderer;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

interface GraphRendererFactory {

    public SvgGraphRenderer createGraphRenderer(int width, int height,
            NodeSizeTransformer nodeSizeTransformer);

    abstract TextBoundsEstimator getTextBoundsEstimator(
            SvgElementFactory svgElementFactory);

    abstract NodeRenderer getNodeRenderer(SvgElementFactory svgElementFactory);

    abstract ArcRenderer getArcRenderer(SvgElementFactory svgElementFactory);

    abstract NodeExpanderRenderer getNodeExpanderRenderer(
            SvgElementFactory svgElementFactory);

}
