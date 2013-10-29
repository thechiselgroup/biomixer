package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;

/**
 * The modern rendering of a node as a circle with text outside of it.
 * 
 * @author elena
 * 
 */
public class CircularTextSvgRenderedNode extends AbstractSvgRenderedNode
        implements Identifiable {

    // private final SvgRectangularExpansionTab expanderTab;

    private final SvgCircleExternalText circleAndText;

    private final SvgElement baseContainer;

    public CircularTextSvgRenderedNode(Node node, SvgElement baseContainer,
            SvgCircleExternalText circleAndText) {
        super(node);
        this.baseContainer = baseContainer;
        baseContainer.appendChild(circleAndText.asSvgElement());
        // baseContainer.appendChild(expanderTab.asSvgElement());
        this.circleAndText = circleAndText;
        // this.expanderTab = expanderTab;
    }

    @Override
    public SvgElement asSvgElement() {
        return baseContainer;
    }

    @Override
    public PointDouble getExpanderPopupLocation() {
        // return getLocation().plus(expanderTab.getLocation());
        return null;
    }

    @Override
    public String getId() {
        return getNode().getId();
    }

    @Override
    public PointDouble getNodeShapeCentre() {
        // Works without width adjustments...container must put circle on its
        // top left corner. I know that the circle gets coordinates of (0,0)
        // internally, so that makes sense.
        return new PointDouble(getLeftX()
        // - circleAndText.getTotalWidth() / 2
                , getTopY()
        // - circleAndText.getTotalHeight() / 2
        );
    }

    // @Override
    // public double getLeftX() {
    // return Double.parseDouble(baseContainer.getAttributeAsString(Svg.X));
    // }
    //
    // @Override
    // public double getTopY() {
    // return Double.parseDouble(baseContainer.getAttributeAsString(Svg.Y));
    // }
    //
    // /**
    // *
    // * @return the coordinates of the top left corner of the node, using the
    // * base svg element's coordinate system
    // */
    // public PointDouble getLocation() {
    // return new PointDouble(Double.parseDouble(baseContainer
    // .getAttributeAsString(Svg.X)), Double.parseDouble(baseContainer
    // .getAttributeAsString(Svg.Y)));
    // }

    /**
     * 
     * @return the coordinates of the top left corner of the node, using the
     *         base svg element's coordinate system
     */
    public PointDouble getLocation() {
        return new PointDouble(getLeftX(), getTopY());
    }

    @Override
    public SizeDouble getSize() {
        return new DefaultSizeDouble(circleAndText.getTotalWidth(),
                circleAndText.getTotalHeight());
    }

    @Override
    public void setSize(SizeDouble size) {
        circleAndText.setCircleRadius(size.getWidth() / 2);
    }

    @Override
    public void setBackgroundColor(String color) {
        circleAndText.setBackgroundColor(color);
    }

    @Override
    public void setBodyEventHandler(ChooselEventHandler handler) {
        circleAndText.setEventListener(handler);
    }

    @Override
    public void setBorderColor(String color) {
        circleAndText.setBorderColor(color);
    }

    @Override
    public void setExpansionEventHandler(ChooselEventHandler handler) {
        // expanderTab.setEventListener(handler);
        // Do nothing, until we want expanders for circular nodes
    }

    @Override
    public void setFontColor(String color) {
        circleAndText.setFontColor(color);
    }

    @Override
    public void setFontWeight(String styleValue) {
        if (styleValue.equals(GraphDisplay.NODE_FONT_WEIGHT_NORMAL)) {
            circleAndText.setFontWeight(Svg.NORMAL);
        } else if (styleValue.equals(GraphDisplay.NODE_FONT_WEIGHT_BOLD)) {
            circleAndText.setFontWeight(Svg.BOLD);
        }
    }

    @Override
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
        baseContainer.setAttribute(Svg.X, x);
        baseContainer.setAttribute(Svg.Y, y);
        updateConnectedArcs();
    }

    @Override
    public VisualItem getVisualItem() {
        return getNode().getVisualItem();
    }
}
