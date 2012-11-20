package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes;

import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

/**
 * Renders a node as a circle with text beside it using
 * {@link SvgCircleExternalText}.
 * 
 * @author elena
 * 
 */
public class CircularTextSvgNodeRenderer implements NodeRenderer {

    public static final double RX_DEFAULT = 10.0;

    public static final double RY_DEFAULT = 10.0;

    private SvgElementFactory svgElementFactory;

    private TextBoundsEstimator textBoundsEstimator;

    // If we want expander tab rendered again, see commit:
    // 8421ecef1062914cf6575bb9e00958ffa0cb1179

    public CircularTextSvgNodeRenderer(SvgElementFactory svgElementFactory,
            TextBoundsEstimator textBoundsEstimator) {
        this.textBoundsEstimator = textBoundsEstimator;
        assert svgElementFactory != null;
        assert textBoundsEstimator != null;
        this.svgElementFactory = svgElementFactory;
    }

    @Override
    public RenderedNode createRenderedNode(Node node) {
        assert node != null;

        SvgElement baseContainer = svgElementFactory.createElement(Svg.SVG);
        baseContainer.setAttribute(Svg.OVERFLOW, Svg.VISIBLE);
        baseContainer.setAttribute(Svg.ID, node.getId());
        baseContainer.setAttribute(Svg.X, 0.0);
        baseContainer.setAttribute(Svg.Y, 0.0);

        SvgCircleExternalText boxedText = new SvgCircleExternalText(
                node.getLabel(), textBoundsEstimator, svgElementFactory,
                node.getSize());

        return new CircularTextSvgRenderedNode(node, baseContainer, boxedText);
    }
}
