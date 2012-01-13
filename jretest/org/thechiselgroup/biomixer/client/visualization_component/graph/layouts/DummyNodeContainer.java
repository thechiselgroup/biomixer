package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

public class DummyNodeContainer {

    public static DummyNodeContainer getDummyNodeContainer(int numberOfDummies) {
        return new DummyNodeContainer(numberOfDummies);
    }

    private final List<Node> dummyNodes = new ArrayList<Node>();

    private final List<NodeItem> dummyNodeItems = new ArrayList<NodeItem>();

    private DummyNodeContainer(int numberOfDummies) {
        for (int i = 0; i < numberOfDummies; i++) {
            createDummyNodes(i);
        }
    }

    private void createDummyNodes(int dummyNumber) {
        Node dummyNode = mock(Node.class);
        when(dummyNode.getId()).thenReturn("" + dummyNumber);
        dummyNodes.add(dummyNode);

        NodeItem dummyNodeItem = mock(NodeItem.class);
        when(dummyNodeItem.getNode()).thenReturn(dummyNode);
        dummyNodeItems.add(dummyNodeItem);
    }

    public Node getDummyNode(int number) {
        return dummyNodes.get(number);
    }

    public NodeItem getDummyNodeItem(int number) {
        return dummyNodeItems.get(number);
    }

    public NodeItem[] getDummyNodeItems() {
        return dummyNodeItems.toArray(new NodeItem[dummyNodeItems.size()]);
    }

}
