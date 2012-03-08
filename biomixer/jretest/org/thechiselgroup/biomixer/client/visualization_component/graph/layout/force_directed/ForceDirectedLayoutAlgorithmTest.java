package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.force_directed;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.AbstractLayoutAlgorithmTest;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.TestLayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.ForceDirectedLayoutAlgorithm;

public class ForceDirectedLayoutAlgorithmTest extends
        AbstractLayoutAlgorithmTest {

    private ForceDirectedLayoutAlgorithm underTest;

    private void computeLayout(TestLayoutGraph graph) {
        LayoutComputation layoutComputation = underTest.computeLayout(graph);
        assertFalse(layoutComputation.isRunning());
    }

    @Before
    public void setUp() {
        underTest = new ForceDirectedLayoutAlgorithm(errorHandler);
    }

    @Ignore("Implement computation")
    @Test
    public void singleNode() {
        createGraph(0, 0, 400, 400);
        LayoutNode[] nodes = createNodes(1);

        computeLayout(graph);

        // TODO: assert at your expected position
        // assertNodeHasCentre(200, 200, nodes[0]);
    }

}
