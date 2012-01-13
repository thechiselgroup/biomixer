package org.thechiselgroup.biomixer.client.visualization_component.graph.layouts;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;

public class DummyArcContainer {

    public static DummyArcContainer getDummyArcContainer(
            String[][] arcSourceTargetIdPairs) {

        return new DummyArcContainer(arcSourceTargetIdPairs);
    }

    private final List<Arc> dummyArcs = new ArrayList<Arc>();

    private final List<ArcItem> dummyArcItems = new ArrayList<ArcItem>();

    private DummyArcContainer(String[][] arcSourceTargetIdPairs) {
        for (int i = 0; i < arcSourceTargetIdPairs.length; i++) {
            String[] arcSourceTargetIdPair = arcSourceTargetIdPairs[i];
            assert arcSourceTargetIdPair.length == 2;
            createArc(arcSourceTargetIdPair);
        }
    }

    private void createArc(String[] arcSourceTargetIdPair) {
        Arc dummyArc = mock(Arc.class);
        when(dummyArc.getSourceNodeId()).thenReturn(arcSourceTargetIdPair[0]);
        when(dummyArc.getTargetNodeId()).thenReturn(arcSourceTargetIdPair[1]);
        dummyArcs.add(dummyArc);

        ArcItem dummyArcItem = mock(ArcItem.class);
        when(dummyArcItem.getArc()).thenReturn(dummyArc);
        dummyArcItems.add(dummyArcItem);
    }

    public Arc getDummyArc(int number) {
        return dummyArcs.get(number);
    }

    public ArcItem getDummyArcItem(int number) {
        return dummyArcItems.get(number);
    }

    public ArcItem[] getDummyArcItems() {
        return dummyArcItems.toArray(new ArcItem[dummyArcItems.size()]);
    }

}
