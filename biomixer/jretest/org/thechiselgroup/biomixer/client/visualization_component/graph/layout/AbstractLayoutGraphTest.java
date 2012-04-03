package org.thechiselgroup.biomixer.client.visualization_component.graph.layout;

import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.DefaultBoundsDouble;

public class AbstractLayoutGraphTest {

    protected TestLayoutGraph graph;

    public AbstractLayoutGraphTest() {
        super();
    }

    protected LayoutArc createArc(int arcType, LayoutNode sourceNode,
            LayoutNode targetNode) {
        return graph.createArc((TestLayoutNode) sourceNode,
                (TestLayoutNode) targetNode, 2, true,
                graph.getTestLayoutArcTypes()[arcType]);
    }

    protected LayoutArc createArc(LayoutNode sourceNode, LayoutNode targetNode) {
        return createArc(0, sourceNode, targetNode);
    }

    protected void createGraph(double leftX, double topY, double width,
            double height) {
        createGraph(leftX, topY, width, height, 1, 1);
    }

    protected void createGraph(double leftX, double topY, double width,
            double height, int numberOfNodeTypes, int numberOfArcTypes) {

        graph = new TestLayoutGraph(new DefaultBoundsDouble(leftX, topY, width,
                height), numberOfNodeTypes, numberOfArcTypes);
    }

    protected TestLayoutNode[] createNodes(int numberOfNodes) {
        return createNodes(0, numberOfNodes);
    }

    protected TestLayoutNode[] createNodes(int nodeType, int numberOfNodes) {
        TestLayoutNodeType testNodeType = graph.getTestLayoutNodeTypes()[nodeType];
        TestLayoutNode[] result = new TestLayoutNode[numberOfNodes];
        for (int i = 0; i < result.length; i++) {
            result[i] = graph.createNode(10, 10, false, testNodeType);
        }
        return result;
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

}