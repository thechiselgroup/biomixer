package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.core.util.text.CanvasTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.SvgBBoxTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.JsDomSvgElementFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.ArcRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeExpanderRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.SvgGraphRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.arcs.StraightLineSvgArcRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.expanders.BoxedTextSvgNodeExpanderRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes.BoxedTextSvgNodeRenderer;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

public class GraphRendererConceptGraphFactory implements GraphRendererFactory {

    final JsDomSvgElementFactory svgElementFactory = new JsDomSvgElementFactory();

    @Override
    public SvgGraphRenderer createGraphRenderer(int width, int height) {
        return new SvgGraphRenderer(width, height, svgElementFactory,
                getNodeRenderer(svgElementFactory),
                getArcRenderer(svgElementFactory),
                getNodeExpanderRenderer(svgElementFactory));
    }

    @Override
    public TextBoundsEstimator getTextBoundsEstimator(
            SvgElementFactory svgElementFactory) {
        return new CanvasTextBoundsEstimator(new SvgBBoxTextBoundsEstimator(
                svgElementFactory));
    }

    @Override
    public NodeRenderer getNodeRenderer(SvgElementFactory svgElementFactory) {
        return new BoxedTextSvgNodeRenderer(svgElementFactory,
                getTextBoundsEstimator(svgElementFactory));
    }

    @Override
    public ArcRenderer getArcRenderer(SvgElementFactory svgElementFactory) {
        return new StraightLineSvgArcRenderer(svgElementFactory);
    }

    @Override
    public NodeExpanderRenderer getNodeExpanderRenderer(
            SvgElementFactory svgElementFactory) {
        return new BoxedTextSvgNodeExpanderRenderer(svgElementFactory,
                getTextBoundsEstimator(svgElementFactory));
    }

}
