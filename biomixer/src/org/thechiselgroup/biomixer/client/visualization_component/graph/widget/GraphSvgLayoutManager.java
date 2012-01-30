package org.thechiselgroup.biomixer.client.visualization_component.graph.widget;

import java.util.Collection;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;

public class GraphSvgLayoutManager {

    public void animateMoveTo(Node node, Point targetLocation) {
        // TODO Auto-generated method stub

    }

    public void runLayout() {
        // TODO Auto-generated method stub

    }

    public void runLayout(String layout) {
        // TODO Auto-generated method stub

    }

    public void runLayoutOnNodes(Collection<Node> nodes) {
        // TODO Auto-generated method stub

    }

    public void setLocation(SvgElement nodeElement, Point location) {
        // XXX note that svg coordinate is for bottom left corner of text, may
        // need some y offset
        SvgElement nodeBox = nodeElement.getChild(0);
        nodeBox.setAttribute(Svg.X, location.getX());
        nodeBox.setAttribute(Svg.Y, location.getY());
    }

}
