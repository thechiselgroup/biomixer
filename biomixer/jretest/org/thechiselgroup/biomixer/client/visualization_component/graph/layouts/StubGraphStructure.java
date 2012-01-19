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

    private final List<NodeItem> stubNodeItems = new ArrayList<NodeItem>();

    private final List<ArcItem> stubArcItems = new ArrayList<ArcItem>();

    public StubGraphStructure(int numberOfNodes) {
        for (int i = 0; i < numberOfNodes; i++) {
            createNode(i);
        }
    }

    public void createArc(int parentId, int childId) {
        Arc stubArc = mock(Arc.class);
        when(stubArc.getTargetNodeId()).thenReturn("" + parentId);
        when(stubArc.getSourceNodeId()).thenReturn("" + childId);

        ArcItem stubArcItem = mock(ArcItem.class);
        when(stubArcItem.getArc()).thenReturn(stubArc);
        stubArcItems.add(stubArcItem);
    }

    private void createNode(int number) {
        Node stubNode = mock(Node.class);
        when(stubNode.getId()).thenReturn("" + number);

        NodeItem stubNodeItem = mock(NodeItem.class);
        when(stubNodeItem.getNode()).thenReturn(stubNode);
        stubNodeItems.add(stubNodeItem);
    }

    public Arc getArc(int number) {
        return getArcItem(number).getArc();
    }

    public ArcItem getArcItem(int number) {
        return stubArcItems.get(number);
    }

    public List<ArcItem> getArcItems() {
        return stubArcItems;
    }

    public Node getNode(int number) {
        return getNodeItem(number).getNode();
    }

    public NodeItem getNodeItem(int number) {
        return stubNodeItems.get(number);
    }

    public List<NodeItem> getNodeItems() {
        return stubNodeItems;
    }

}
