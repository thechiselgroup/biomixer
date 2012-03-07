/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph.widget;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.util.collections.ArrayUtils;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;

import pl.rmalinowski.gwt2swf.client.ui.SWFWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/*
 * IMPLEMENTATION NOTE: We use try-catch blocks around calls into JavaScript, 
 * because the argument values and the actual calling method are not reported 
 * in JavaScriptException that occur in GWT hosted mode.
 */
public class GraphWidget extends SWFWidget implements GraphDisplay {

    public static class Location extends JavaScriptObject {

        protected Location() {
        }

        // @formatter:off
        public final native double getX() /*-{
			return this.x;
        }-*/;

        public final native double getY() /*-{
			return this.y;
        }-*/;
        // @formatter:on

    }

    private static final String CSS_GRAPH_LOADING_INFO = "graph-loading-info";

    public static final String FLASH_VAR_SWFID = "swfid";

    public static final String SWF_FILE = GWT.getModuleBaseURL()
            + "swf/BasicGraph.swf";

    private static Map<String, GraphWidget> widgets = CollectionFactory
            .createStringMap();

    static {
        try {
            exportStaticMethods();
        } catch (Exception ex) {
            // XXX use initializables & dependency injection
            Logger.getLogger("").log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    // @formatter:off
    private static native void _addArc(String swfID, String arcId,
            String sourceNodeId, String targetNodeId, String type, boolean directed) /*-{
		$doc.getElementById(swfID).addArc(arcId, sourceNodeId, targetNodeId,
				type, directed);
    }-*/;
    // @formatter:on

    /*
     * IMPLEMENTATION NOTE: There is a bug in the JavaScript to Flex conversion
     * for compiled GWT code that causes the label to appear as [Object object]
     * in the Flex graph if it is passed in directly. That is why we create a
     * separate JavaScript String and append the label String passed in from
     * GWT.
     */
    // @formatter:off
    private static native void _addNode(String swfID, String id, String type,
            String label) /*-{
		var jsLabel = "" + label;
		$doc.getElementById(swfID).addNode(id, type, jsLabel);
    }-*/;
    // @formatter:on

    // @formatter:off
    private static native void _addNodeMenuItem(String swfID, String itemId,
            String itemLabel, String nodeType) /*-{
		$doc.getElementById(swfID).addNodeMenuItem(
				"_flexvis_onNodeMenuItemClicked", itemId, itemLabel, nodeType);
    }-*/;
    // @formatter:on

    private static GraphWidget _getGraphWidgetByID(String swfID) {
        return GraphWidget.widgets.get(swfID);
    }

    // @formatter:off
    private static native Location _getNodeLocation(String swfID, String nodeId) /*-{
		return $doc.getElementById(swfID).getNodeLocation(nodeId);
    }-*/;
    // @formatter:on

    public static void _log(String message) {
        // XXX use initializables & dependency injection
        Logger.getLogger("").log(Level.WARNING, message);
    }

    public static void _onArcMouseClick(String arcID, int mouseX, int mouseY,
            String swfID) {
        _getGraphWidgetByID(swfID).onArcMouseClick(arcID, mouseX, mouseY);
    }

    public static void _onArcMouseDoubleClick(String arcID, int mouseX,
            int mouseY, String swfID) {
        _getGraphWidgetByID(swfID).onArcMouseDoubleClick(arcID, mouseX, mouseY);
    }

    public static void _onArcMouseOut(String arcID, int mouseX, int mouseY,
            String swfID) {
        _getGraphWidgetByID(swfID).onArcMouseOut(arcID, mouseX, mouseY);
    }

    public static void _onArcMouseOver(String arcID, int mouseX, int mouseY,
            String swfID) {
        _getGraphWidgetByID(swfID).onArcMouseOver(arcID, mouseX, mouseY);
    }

    public static void _onLoad(String swfID) {
        try {
            _registerFlexHooks(swfID);
            _getGraphWidgetByID(swfID).onWidgetReady();
        } catch (Exception ex) {
            _getGraphWidgetByID(swfID).onLoadFailure(ex);
        }
    }

    /*
     * NOTE: FlexViz returns double for the locations.
     */
    public static void _onNodeDrag(String nodeID, double startX, double startY,
            double endX, double endY, String swfID) {

        _getGraphWidgetByID(swfID).onNodeDrag(nodeID, (int) startX,
                (int) startY, (int) endX, (int) endY);
    }

    public static void _onNodeDragHandleMouseDown(String nodeID, int mouseX,
            int mouseY, String swfID) {

        _getGraphWidgetByID(swfID).onNodeDragHandleMouseDown(nodeID, mouseX,
                mouseY);
    }

    public static void _onNodeDragHandleMouseMove(String nodeID, int mouseX,
            int mouseY, String swfID) {

        _getGraphWidgetByID(swfID).onNodeDragHandleMouseMove(nodeID, mouseX,
                mouseY);
    }

    public static void _onNodeMenuItemClicked(String itemId, String nodeLabel,
            String nodeId, int x, int y, String swfID) {
        _getGraphWidgetByID(swfID).onNodeMenuItemClicked(itemId, nodeId);
    }

    public static void _onNodeMouseClick(String nodeID, int mouseX, int mouseY,
            String swfID) {
        _getGraphWidgetByID(swfID).onNodeMouseClick(nodeID, mouseX, mouseY);
    }

    public static void _onNodeMouseDoubleClick(String nodeID, int mouseX,
            int mouseY, String swfID) {
        _getGraphWidgetByID(swfID).onNodeMouseDoubleClick(nodeID, mouseX,
                mouseY);
    }

    public static void _onNodeMouseOut(String nodeID, int mouseX, int mouseY,
            String swfID) {
        _getGraphWidgetByID(swfID).onNodeMouseOut(nodeID, mouseX, mouseY);
    }

    public static void _onNodeMouseOver(String nodeID, int mouseX, int mouseY,
            String swfID) {
        _getGraphWidgetByID(swfID).onNodeMouseOver(nodeID, mouseX, mouseY);
    }

    // @formatter:off
    private static native void _registerFlexHooks(String swfID) /*-{
		var flexWidget = $doc.getElementById(swfID);

		flexWidget.addNodeMouseOverListener("_flexvis_nodeMouseOver");
		flexWidget.addNodeMouseOutListener("_flexvis_nodeMouseOut");
		flexWidget.addNodeMouseClickListener("_flexvis_nodeMouseClick");
		flexWidget
				.addNodeMouseDoubleClickListener("_flexvis_nodeMouseDoubleClick");

		flexWidget
				.addNodeDragHandleMouseDownListener("_flexvis_onNodeDragHandleMouseDown");
		flexWidget
				.addNodeDragHandleMouseMoveListener("_flexvis_onNodeDragHandleMouseMove");

		flexWidget.addArcMouseOverListener("_flexvis_arcMouseOver");
		flexWidget.addArcMouseOutListener("_flexvis_arcMouseOut");
		flexWidget.addArcMouseClickListener("_flexvis_arcMouseClick");
		flexWidget
				.addArcMouseDoubleClickListener("_flexvis_arcMouseDoubleClick");

		flexWidget.addNodeDragListener("_flexvis_nodeDrag");
    }-*/;

    private static native void _removeArc(String swfID, String arcId) /*-{
		$doc.getElementById(swfID).removeArc(arcId);
    }-*/;

    private static native void _removeNode(String swfID, String nodeId) /*-{
		$doc.getElementById(swfID).removeNode(nodeId);
    }-*/;

    private static native void _runLayout(String swfID, String layoutName) /*-{
		$doc.getElementById(swfID).runLayout(layoutName);
    }-*/;

    /**
     * @param nodeIds
     *            JavaScript array with node ids (cannot use java array - this
     *            is opaque to java script)
     */
    private static native void _runLayout(String swfID, String layoutName,
            JavaScriptObject nodeIds) /*-{
		$doc.getElementById(swfID).runLayout(layoutName, nodeIds);
    }-*/;

    private static native void _setArcStyle(String swfID, String arcId,
            String styleProp, String styleValue) /*-{
		$doc.getElementById(swfID).setArcStyle(arcId, styleProp, styleValue);
    }-*/;

    private static native Location _setNodeLocation(String swfID,
            String nodeId, int x, int y) /*-{
		return $doc.getElementById(swfID).setNodeLocation(nodeId, x, y);
    }-*/;

    private static native void _setNodeLocation(String swfID, String nodeId,
            int x, int y, boolean animate) /*-{
		$doc.getElementById(swfID).setNodeLocation(nodeId, x, y, animate);
    }-*/;

    /*
     * public function setNodeStyle(nodeID:String, styleProp:String,
     * styleValue:Object):void
     */
    private static native void _setNodeStyle(String swfID, String nodeId,
            String styleProp, String styleValue) /*-{
		$doc.getElementById(swfID).setNodeStyle(nodeId, styleProp, styleValue);
    }-*/;

    private static native void exportStaticMethods() /*-{
        $wnd._flexvis_loaded=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onLoad(Ljava/lang/String;));

        $wnd._flexvis_log=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_log(Ljava/lang/String;));

        $wnd._flexvis_nodeMouseOver=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onNodeMouseOver(Ljava/lang/String;IILjava/lang/String;));
        $wnd._flexvis_nodeMouseOut=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onNodeMouseOut(Ljava/lang/String;IILjava/lang/String;));
        $wnd._flexvis_nodeMouseClick=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onNodeMouseClick(Ljava/lang/String;IILjava/lang/String;));
        $wnd._flexvis_nodeMouseDoubleClick=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onNodeMouseDoubleClick(Ljava/lang/String;IILjava/lang/String;));

        $wnd._flexvis_arcMouseOver=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onArcMouseOver(Ljava/lang/String;IILjava/lang/String;));
        $wnd._flexvis_arcMouseOut=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onArcMouseOut(Ljava/lang/String;IILjava/lang/String;));
        $wnd._flexvis_arcMouseClick=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onArcMouseClick(Ljava/lang/String;IILjava/lang/String;));
        $wnd._flexvis_arcMouseDoubleClick=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onArcMouseDoubleClick(Ljava/lang/String;IILjava/lang/String;));

        $wnd._flexvis_onNodeMenuItemClicked=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onNodeMenuItemClicked(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IILjava/lang/String;));
        $wnd._flexvis_onNodeDragHandleMouseDown=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onNodeDragHandleMouseDown(Ljava/lang/String;IILjava/lang/String;))
        $wnd._flexvis_onNodeDragHandleMouseMove=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onNodeDragHandleMouseMove(Ljava/lang/String;IILjava/lang/String;))

        $wnd._flexvis_nodeDrag=$entry(
        @org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphWidget::_onNodeDrag(Ljava/lang/String;DDDDLjava/lang/String;));
    }-*/;
    // @formatter:on

    private final Map<String, Arc> arcsByID = CollectionFactory
            .createStringMap();

    private Element loadingInfoDiv;

    private final Map<String, NodeMenuItemClickedHandler> nodeMenuItemClickHandlers = CollectionFactory
            .createStringMap();

    private int nodeMenuItemIdCounter = 0;

    private final Map<String, Node> nodesByID = CollectionFactory
            .createStringMap();

    public GraphWidget(int width, int height) {
        super(SWF_FILE, width, height);

        addAttribute("wmode", "transparent");

        // hack around IE / FF differences with Flash embedding
        addFlashVar(FLASH_VAR_SWFID, getSwfId());
    }

    @Override
    public void addArc(Arc arc) {
        assert arc != null;
        assert !arcsByID.containsKey(arc.getId()) : "arc '" + arc.getId()
                + "' must not be already contained";
        assert nodesByID.containsKey(arc.getSourceNodeId()) : "source node '"
                + arc.getSourceNodeId() + "' must be available";
        assert nodesByID.containsKey(arc.getTargetNodeId()) : "target node '"
                + arc.getTargetNodeId() + "'must be available";

        try {
            arcsByID.put(arc.getId(), arc);
            _addArc(getSwfId(), arc.getId(), arc.getSourceNodeId(),
                    arc.getTargetNodeId(), arc.getType(), arc.isDirected());
        } catch (Exception ex) {
            throw new GraphWidgetException("addArc(" + arc + ") failed", ex);
        }
    }

    @Override
    public <T extends EventHandler> HandlerRegistration addEventHandler(
            Type<T> type, T handler) {

        assert type != null;
        assert handler != null;

        if (type instanceof DomEvent.Type) {
            return addDomHandler(handler, (DomEvent.Type<T>) type);
        } else {
            return addHandler(handler, type);
        }
    }

    @Override
    public HandlerRegistration addGraphDisplayLoadingFailureHandler(
            GraphDisplayLoadingFailureEventHandler handler) {

        assert handler != null;

        return addHandler(handler, GraphDisplayLoadingFailureEvent.TYPE);
    }

    @Override
    public HandlerRegistration addGraphDisplayReadyHandler(
            GraphDisplayReadyEventHandler handler) {

        assert handler != null;

        return addHandler(handler, GraphDisplayReadyEvent.TYPE);
    }

    @Override
    public void addNode(Node node) {
        assert node != null;
        assert !nodesByID.containsKey(node.getId()) : "node must not be contained";

        try {
            _addNode(getSwfId(), node.getId(), node.getType(), node.getLabel());
            nodesByID.put(node.getId(), node);
        } catch (Exception ex) {
            throw new GraphWidgetException("addNode(" + node + ") failed", ex);
        }
    }

    @Override
    public void addNodeMenuItemHandler(String menuLabel,
            NodeMenuItemClickedHandler handler, String nodeType) {

        assert menuLabel != null;
        assert handler != null;
        assert nodeType != null;

        String id = "menuItemId-" + (nodeMenuItemIdCounter++);
        try {
            nodeMenuItemClickHandlers.put(id, handler);
            _addNodeMenuItem(getSwfId(), id, menuLabel, nodeType);
        } catch (Exception ex) {
            throw new GraphWidgetException("addNodeMenuItemHandler(" + id + ","
                    + menuLabel + "," + nodeType + ") failed", ex);
        }
    }

    @Override
    public void animateMoveTo(Node node, Point targetLocation) {
        assert node != null;
        assert containsNode(node.getId());
        assert targetLocation != null;

        try {
            _setNodeLocation(getSwfId(), node.getId(), targetLocation.getX(),
                    targetLocation.getY(), true);
        } catch (Exception ex) {
            throw new GraphWidgetException("animateMoveTo(" + node + ","
                    + targetLocation + ") failed", ex);
        }

    }

    @Override
    public Widget asWidget() {
        return this;
    }

    private void attachLoadingStateInformation() {
        loadingInfoDiv = DOM.createDiv();
        loadingInfoDiv.setInnerText("Graph viewer loading...");
        loadingInfoDiv.addClassName(CSS_GRAPH_LOADING_INFO);
        getElement().appendChild(loadingInfoDiv);
    }

    @Override
    public boolean containsArc(String arcId) {
        assert arcId != null;
        return arcsByID.containsKey(arcId);
    }

    @Override
    public boolean containsNode(String nodeId) {
        assert nodeId != null;
        return nodesByID.containsKey(nodeId);
    }

    private void detachLoadingStateInformation() {
        getElement().removeChild(loadingInfoDiv);
    }

    @Override
    public Arc getArc(String arcId) {
        assert arcId != null;
        assert arcsByID.containsKey(arcId);

        return arcsByID.get(arcId);
    }

    @Override
    public Point getLocation(Node node) {
        assert node != null;
        assert containsNode(node.getId());

        try {
            Location result = _getNodeLocation(getSwfId(), node.getId());
            return new Point((int) result.getX(), (int) result.getY());
        } catch (Exception ex) {
            throw new GraphWidgetException("getLocation(" + node + ") failed",
                    ex);
        }
    }

    @Override
    public Node getNode(String nodeId) {
        assert nodeId != null;
        assert nodesByID.containsKey(nodeId) : "node '" + nodeId
                + "' must be contained";

        return nodesByID.get(nodeId);
    }

    private void onArcMouseClick(String arcID, int mouseX, int mouseY) {
        int x = getAbsoluteLeft() + mouseX;
        int y = getAbsoluteTop() + mouseY;

        fireEvent(new ArcMouseClickEvent(getArc(arcID), x, y));
    }

    private void onArcMouseDoubleClick(String arcID, int mouseX, int mouseY) {
        int x = getAbsoluteLeft() + mouseX;
        int y = getAbsoluteTop() + mouseY;

        fireEvent(new ArcMouseDoubleClickEvent(getArc(arcID), x, y));
    }

    private void onArcMouseOut(String arcID, int mouseX, int mouseY) {
        int x = getAbsoluteLeft() + mouseX;
        int y = getAbsoluteTop() + mouseY;

        fireEvent(new ArcMouseOutEvent(getArc(arcID), x, y));
    }

    private void onArcMouseOver(String arcID, int mouseX, int mouseY) {
        int x = getAbsoluteLeft() + mouseX;
        int y = getAbsoluteTop() + mouseY;

        fireEvent(new ArcMouseOverEvent(getArc(arcID), x, y));
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        attachLoadingStateInformation();
        GraphWidget.widgets.put(getSwfId(), this);
    }

    private void onLoadFailure(Exception ex) {
        loadingInfoDiv.setInnerText("Loading SWF failed: " + ex.getMessage());
        fireEvent(new GraphDisplayLoadingFailureEvent(this, ex));
    }

    private void onNodeDrag(String nodeID, int startX, int startY, int endX,
            int endY) {

        fireEvent(new NodeDragEvent(getNode(nodeID), startX, startY, endX, endY));
    }

    private void onNodeDragHandleMouseDown(String nodeID, int mouseX, int mouseY) {
        fireEvent(new NodeDragHandleMouseDownEvent(getNode(nodeID), mouseX,
                mouseY));
    }

    private void onNodeDragHandleMouseMove(String nodeID, int mouseX, int mouseY) {
        fireEvent(new NodeDragHandleMouseMoveEvent(getNode(nodeID), mouseX,
                mouseY));
    }

    private void onNodeMenuItemClicked(String itemId, String nodeId) {
        assert itemId != null;
        assert nodeId != null;

        Node node = getNode(nodeId);
        assert node != null;

        NodeMenuItemClickedHandler handler = nodeMenuItemClickHandlers
                .get(itemId);
        assert handler != null;

        handler.onNodeMenuItemClicked(node);
    }

    private void onNodeMouseClick(String nodeID, int mouseX, int mouseY) {
        int x = getAbsoluteLeft() + mouseX;
        int y = getAbsoluteTop() + mouseY;

        fireEvent(new NodeMouseClickEvent(getNode(nodeID), x, y));
    }

    private void onNodeMouseDoubleClick(String nodeID, int mouseX, int mouseY) {
        int x = getAbsoluteLeft() + mouseX;
        int y = getAbsoluteTop() + mouseY;

        fireEvent(new NodeMouseDoubleClickEvent(getNode(nodeID), x, y));
    }

    private void onNodeMouseOut(String nodeID, int mouseX, int mouseY) {
        int x = getAbsoluteLeft() + mouseX;
        int y = getAbsoluteTop() + mouseY;

        fireEvent(new NodeMouseOutEvent(getNode(nodeID), x, y));
    }

    private void onNodeMouseOver(String nodeID, int mouseX, int mouseY) {
        int x = getAbsoluteLeft() + mouseX;
        int y = getAbsoluteTop() + mouseY;

        fireEvent(new NodeMouseOverEvent(getNode(nodeID), x, y));
    }

    @Override
    protected void onUnload() {
        GraphWidget.widgets.remove(getSwfId());

        super.onUnload();
    }

    private void onWidgetReady() {
        detachLoadingStateInformation();
        fireEvent(new GraphDisplayReadyEvent(this));
    }

    @Override
    public void removeArc(Arc arc) {
        assert arc != null;
        assert containsArc(arc.getId());

        try {
            _removeArc(getSwfId(), arc.getId());
            arcsByID.remove(arc.getId());
        } catch (Exception ex) {
            throw new GraphWidgetException("removeArc(" + arc + ") failed", ex);
        }
    }

    @Override
    public void removeNode(Node node) {
        assert node != null;
        assert containsNode(node.getId());

        try {
            _removeNode(getSwfId(), node.getId());
            nodesByID.remove(node.getId());
        } catch (Exception ex) {
            throw new GraphWidgetException("removeNode(" + node + ") failed",
                    ex);
        }
    }

    @Override
    public void runLayout() throws LayoutException {
        runLayout(DEFAULT_LAYOUT);
    }

    @Override
    public LayoutComputation runLayout(LayoutAlgorithm layoutAlgorithm) {
        // XXX this Flash widget cannot run the new algorithms. This widget will
        // be removed.
        return null;
    }

    @Override
    public void runLayout(String layout) throws LayoutException {
        assert layout != null;

        try {
            _runLayout(getSwfId(), layout);
        } catch (JavaScriptException ex) {
            throw new LayoutException(layout, ex);
        }
    }

    @Override
    public void runLayoutOnNodes(Collection<Node> nodes) throws LayoutException {
        assert nodes != null;

        if (nodes.size() == 0) {
            return;
        }

        try {
            _runLayout(getSwfId(), DEFAULT_LAYOUT,
                    ArrayUtils.toJsArray(toNodeIdArray(nodes)));
        } catch (JavaScriptException ex) {
            throw new LayoutException(DEFAULT_LAYOUT, ex);
        }
    }

    @Override
    public void setArcStyle(Arc arc, String styleProperty, String styleValue) {
        assert arc != null;
        assert containsArc(arc.getId());
        assert styleProperty != null;
        assert styleValue != null;

        try {
            _setArcStyle(getSwfId(), arc.getId(), styleProperty, styleValue);
        } catch (Exception ex) {
            throw new GraphWidgetException("setArcStyle(" + arc + ","
                    + styleProperty + "," + styleValue + ") failed", ex);
        }
    }

    @Override
    public void setLocation(Node node, Point point) {
        assert node != null;
        assert containsNode(node.getId());
        assert point != null;

        try {
            _setNodeLocation(getSwfId(), node.getId(), point.getX(),
                    point.getY());
        } catch (Exception ex) {
            throw new GraphWidgetException("setLocation(" + node + "," + point
                    + ") failed", ex);
        }
    }

    @Override
    public void setNodeStyle(Node node, String styleProperty, String styleValue) {
        assert node != null;
        assert containsNode(node.getId());
        assert styleProperty != null;
        assert styleValue != null;

        try {
            _setNodeStyle(getSwfId(), node.getId(), styleProperty, styleValue);
        } catch (Exception ex) {
            throw new GraphWidgetException("setNodeStyle(" + node + ","
                    + styleProperty + "," + styleValue + ") failed", ex);
        }
    }

    private String[] toNodeIdArray(Collection<Node> nodes) {
        String[] nodeIds = new String[nodes.size()];
        int i = 0;
        for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
            nodeIds[i++] = it.next().getId();

        }
        return nodeIds;
    }

}