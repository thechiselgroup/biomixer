package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.core.util.text.TestTextBoundsEstimator;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.NodeSizeTransformer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.SvgGraphRenderer;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;
import org.thechiselgroup.biomixer.shared.svg.text_renderer.TextSvgElementFactory;

public class GraphRendererOntologyOverviewGraphTestFactory extends
        GraphRendererOntologyOverviewFactory {

    /*
     * Special svg elements for testing
     */
    final TextSvgElementFactory svgElementFactory = new TextSvgElementFactory();

    final GraphElementSizeTransformerFactory sizeTransformerFactory = new GraphElementSizeTransformerFactory();

    @Override
    public SvgGraphRenderer createGraphRenderer(int width, int height) {
        return new SvgGraphRenderer(width, height, svgElementFactory,
                getNodeRenderer(svgElementFactory),
                getArcRenderer(svgElementFactory),
                getNodeExpanderRenderer(svgElementFactory),
                sizeTransformerFactory.createOntologyNodeSizeTransformer(),
                sizeTransformerFactory
                        .createOntologyMappingArcSizeTransformer());
    }

    /**
     * Special bounds estimator for testing
     */
    @Override
    public TextBoundsEstimator getTextBoundsEstimator(
            SvgElementFactory svgElementFactory) {
        return new TestTextBoundsEstimator(10, 20);
    }

}
