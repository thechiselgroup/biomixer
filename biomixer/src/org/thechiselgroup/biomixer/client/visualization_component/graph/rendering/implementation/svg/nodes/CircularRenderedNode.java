package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
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
public class CircularRenderedNode extends AbstractSvgRenderedNode implements
        Identifiable {

	// private final SvgRectangularExpansionTab expanderTab;
	
    private final SvgCircleWithText boxedText;

    private final SvgElement baseContainer;

    public CircularRenderedNode(Node node, SvgElement baseContainer,
            SvgCircleWithText boxedText) {
        super(node);
        this.baseContainer = baseContainer;
        baseContainer.appendChild(boxedText.asSvgElement());
        // baseContainer.appendChild(expanderTab.asSvgElement());
        this.boxedText = boxedText;
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

//    @Override
//    public double getLeftX() {
//        return Double.parseDouble(baseContainer.getAttributeAsString(Svg.X));
//    }
//
//    @Override
//    public double getTopY() {
//        return Double.parseDouble(baseContainer.getAttributeAsString(Svg.Y));
//    }
//    
//    /**
//     * 
//     * @return the coordinates of the top left corner of the node, using the
//     *         base svg element's coordinate system
//     */
//    public PointDouble getLocation() {
//        return new PointDouble(Double.parseDouble(baseContainer
//                .getAttributeAsString(Svg.X)), Double.parseDouble(baseContainer
//                .getAttributeAsString(Svg.Y)));
//    }
    
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
        return new DefaultSizeDouble(boxedText.getTotalWidth(),
                boxedText.getTotalHeight());
    }



    @Override
    public void setBackgroundColor(String color) {
        boxedText.setBackgroundColor(color);
    }

    @Override
    public void setBodyEventHandler(ChooselEventHandler handler) {
        boxedText.setEventListener(handler);
    }

    @Override
    public void setBorderColor(String color) {
        boxedText.setBorderColor(color);
    }

    @Override
    public void setExpansionEventHandler(ChooselEventHandler handler) {
        // expanderTab.setEventListener(handler);
        // Do nothing, until we want expanders for circular nodes
    }

    @Override
    public void setFontColor(String color) {
        boxedText.setFontColor(color);
    }

    @Override
    public void setFontWeight(String styleValue) {
        if (styleValue.equals(GraphDisplay.NODE_FONT_WEIGHT_NORMAL)) {
            boxedText.setFontWeight(Svg.NORMAL);
        } else if (styleValue.equals(GraphDisplay.NODE_FONT_WEIGHT_BOLD)) {
            boxedText.setFontWeight(Svg.BOLD);
        }
    }

    @Override
    public void setPosition(double x, double y) {
    	super.setPosition(x, y);
        baseContainer.setAttribute(Svg.X, x);
        baseContainer.setAttribute(Svg.Y, y);
        // TODO Is this call necessary? Try commenting it out.
        updateConnectedArcs();
    }
}
