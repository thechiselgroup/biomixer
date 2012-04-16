package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.expanders;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionUtils;
import org.thechiselgroup.biomixer.client.core.util.text.TextBoundsEstimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeExpanderRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes.BoxedTextSvgComponent;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;
import org.thechiselgroup.biomixer.shared.svg.SvgUtils;

/**
 * Renders the node expander as stacked rectangles with text in them.
 * 
 * @author drusk
 * 
 */
public class DefaultSvgNodeExpanderRenderer implements NodeExpanderRenderer {

    private SvgElementFactory svgElementFactory;

    private TextBoundsEstimator textBoundsEstimator;

    public DefaultSvgNodeExpanderRenderer(SvgElementFactory svgElementFactory,
            TextBoundsEstimator textBoundsEstimator) {
        assert svgElementFactory != null;
        assert textBoundsEstimator != null;
        this.svgElementFactory = svgElementFactory;
        this.textBoundsEstimator = textBoundsEstimator;
    }

    @Override
    public RenderedNodeExpander renderNodeExpander(PointDouble topLeftLocation,
            Set<String> expanderLabels) {
        SvgElement popUpContainer = svgElementFactory.createElement(Svg.SVG);
        popUpContainer.setAttribute(Svg.OVERFLOW, Svg.VISIBLE);
        SvgUtils.setXY(popUpContainer, topLeftLocation);

        Map<String, BoxedTextSvgComponent> boxedTextEntries = CollectionFactory
                .createStringMap();

        double maxWidth = 0.0;
        // sort by label so that expanders always show up in a consistent order
        List<String> sortedExpanderLabels = CollectionUtils
                .asSortedList(expanderLabels);
        for (String expanderId : sortedExpanderLabels) {
            BoxedTextSvgComponent boxedText = new BoxedTextSvgComponent(
                    expanderId, textBoundsEstimator, svgElementFactory);
            boxedTextEntries.put(expanderId, boxedText);
            double boxWidth = boxedText.getTotalWidth();
            if (boxWidth > maxWidth) {
                maxWidth = boxWidth;
            }
        }

        double currentOffsetY = 0;
        for (String expanderId : sortedExpanderLabels) {
            BoxedTextSvgComponent boxedText = boxedTextEntries.get(expanderId);
            boxedText.setBoxWidth(maxWidth);
            boxedText.setY(currentOffsetY);
            popUpContainer.appendChild(boxedText.asSvgElement());
            currentOffsetY += boxedText.getTotalHeight();
        }

        return new PopupExpanderSvgComponent(popUpContainer, boxedTextEntries);
    }

}
