package org.thechiselgroup.biomixer.client.visualization_component.graph.widget;

import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

public class GraphSvgComponentManager {

    private SvgElementFactory svgElementFactory;

    private Map<String, Arc> arcsById = CollectionFactory.createStringMap();

    private Map<String, Node> nodesById = CollectionFactory.createStringMap();

    private SvgElement rootElement;

    public GraphSvgComponentManager(SvgElementFactory svgElementFactory) {
        this.svgElementFactory = svgElementFactory;
        rootElement = svgElementFactory.createElement(Svg.SVG);
        rootElement.setAttribute("xmlns", Svg.NAMESPACE);
        rootElement.setAttribute("version", "1.1");
    }

    public void addArc(Arc arc) {
        assert arc != null;
        assert !arcsById.containsKey(arc.getId()) : "arc '" + arc.getId()
                + "'must not be already contained";
        assert nodesById.containsKey(arc.getSourceNodeId()) : "source node '"
                + arc.getSourceNodeId() + "' must be available";
        assert nodesById.containsKey(arc.getTargetNodeId()) : "target node '"
                + arc.getTargetNodeId() + "' must be available";

        // add svg element
        SvgElement line = svgElementFactory.createElement(Svg.LINE);
        arcsById.put(arc.getId(), arc);
    }

    public SvgElement addNode(Node node) {
        assert node != null;
        assert !nodesById.containsKey(node.getId()) : "node must not be contained";

        // create G element for rect, text, drop down, etc
        SvgElement grouping = svgElementFactory.createElement(Svg.G);
        grouping.setAttribute(Svg.ID, node.getId());

        double defaultWidth = 100;
        double defaultHeight = 40;
        double rx = 10;
        double ry = 10;
        // add svg element
        // 3. create rectangle at some padded amount around text
        SvgElement rectangle = svgElementFactory.createElement(Svg.RECT);
        rectangle.setAttribute(Svg.FILL, "none");
        // TODO proper colors
        rectangle.setAttribute(Svg.STROKE, "black");
        rectangle.setAttribute(Svg.RX, rx);
        rectangle.setAttribute(Svg.RY, ry);
        rectangle.setAttribute(Svg.WIDTH, defaultWidth);
        rectangle.setAttribute(Svg.HEIGHT, defaultHeight);
        // XXX these are really properties of the style attribute, how do I set
        // them properly??
        // TODO: use proper colours
        // rectangle.setAttribute(Svg.FILL, Colors.BLUE_1);
        // rectangle.setAttribute(Svg.STROKE_WIDTH, 3);
        // rectangle.setAttribute(Svg.STROKE, Colors.BLACK);
        // create a special node svg element with setSize, setVisible, etc.
        // it will just be a rectangle to start with

        // SvgElement text = svgElementFactory.createElement(Svg.TEXT);
        // text.setTextContent(node.getLabel());
        // text.setAttribute(Svg.X, 10);
        // text.setAttribute(Svg.Y, 10);

        grouping.appendChild(rectangle);
        // TODO: add in text, etc

        rootElement.appendChild(grouping);
        nodesById.put(node.getId(), node);
        return grouping;
    }

    public boolean containsArc(String arcId) {
        assert arcId != null;
        return arcsById.containsKey(arcId);
    }

    public boolean containsNode(String nodeId) {
        assert nodeId != null;
        return nodesById.containsKey(nodeId);
    }

    public Arc getArc(String arcId) {
        assert arcId != null;
        assert arcsById.containsKey(arcId);
        return arcsById.get(arcId);
    }

    public Node getNode(String nodeId) {
        assert nodeId != null;
        assert nodesById.containsKey(nodeId);
        return nodesById.get(nodeId);
    }

    public SvgElement getRootElement() {
        return rootElement;
    }

    public void removeArc(Arc arc) {
        // TODO Auto-generated method stub

    }

    public void removeNode(Node node) {
        // TODO Auto-generated method stub
        // XXX if you remove a node, should probably remove arcs pointing to it
    }

    public void setArcStyle(Arc arc, String styleProperty, String styleValue) {
        // TODO Auto-generated method stub

    }

    public void setNodeStyle(Node node, String styleProperty, String styleValue) {
        // TODO Auto-generated method stub

    }

}
