package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

public class StubGraphStructure {

    private final List<Node> stubNodes = new ArrayList<Node>();

    private final List<NodeItem> stubNodeItems = new ArrayList<NodeItem>();

    private final List<Arc> stubArcs = new ArrayList<Arc>();

    private final List<ArcItem> stubArcItems = new ArrayList<ArcItem>();

    public StubGraphStructure(int numberOfNodes) {
        for (int i = 0; i < numberOfNodes; i++) {
            createNode(i);
        }
    }

    public void createArc(int sourceId, int targetId) {
        Arc stubArc = mock(Arc.class);
        when(stubArc.getSourceNodeId()).thenReturn("" + sourceId);
        when(stubArc.getTargetNodeId()).thenReturn("" + targetId);
        stubArcs.add(stubArc);

        ArcItem stubArcItem = mock(ArcItem.class);
        when(stubArcItem.getArc()).thenReturn(stubArc);
        stubArcItems.add(stubArcItem);
    }

    private void createNode(int number) {
        Node stubNode = mock(Node.class);
        when(stubNode.getId()).thenReturn("" + number);
        stubNodes.add(stubNode);

        NodeItem stubNodeItem = mock(NodeItem.class);
        when(stubNodeItem.getNode()).thenReturn(stubNode);
        stubNodeItems.add(stubNodeItem);
    }

    public Arc getArc(int number) {
        return stubArcs.get(number);
    }

    public ArcItem getArcItem(int number) {
        return stubArcItems.get(number);
    }

    public ArcItem[] getArcItems() {
        return stubArcItems.toArray(new ArcItem[stubArcItems.size()]);
    }

    public Node getNode(int number) {
        return stubNodes.get(number);
    }

    public NodeItem getNodeItem(int number) {
        return stubNodeItems.get(number);
    }

    public NodeItem[] getNodeItems() {
        return stubNodeItems.toArray(new NodeItem[stubNodeItems.size()]);
    }

}
