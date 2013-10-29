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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.thechiselgroup.biomixer.client.DataTypeValidator;
import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.command.UndoableCommand;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.ui.Color;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionUtils;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.DefaultVisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.TestViewContentDisplayCallback;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemTestUtils;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.GraphDisplayController;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.ArcSettings;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeDragEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeDragHandler;
import org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers;

import com.google.gwt.user.client.Event;

public class GraphViewContentDisplayTest {

    @Mock
    private ArcTypeProvider arcStyleProvider;

    @Mock
    private NodeExpander automaticExpander;

    @Mock
    private NodeBulkExpander automaticBulkExpander;

    private TestViewContentDisplayCallback callback;

    @Mock
    private CommandManager commandManager;

    private Graph underTest;

    @Mock
    private GraphDisplayController graphDisplay;

    @Mock
    VisualItem visualItem;

    @Mock
    private Node node;

    @Mock
    private GraphExpansionRegistry registry;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private DataTypeValidator dataTypeValidator;

    @Mock
    private ResourceCategorizer resourceCategorizer;

    @Mock
    private ResourceManager resourceManager;

    private Point sourceLocation;

    private Point targetLocation;

    @Mock
    ChooselEvent chooselEvent;

    @Mock
    Event browserEvent;

    @Mock
    private ArcType arcType;

    private String arcTypeId;

    private String arcLabel;

    private boolean arcDirected;

    private Color arcColor;

    private int arcThickness;

    private String arcStyle;

    private String arcHead;

    private Object borderColor;

    private Object backgroundColor;

    private VisualItemContainer visualItemContainer;

    @Test
    public void addResourceItemsCallsArcTypeGetArcItems() {
        arcStyleProviderReturnArcType();
        init();
        arcTypeReturnsArcs(any(VisualItem.class));

        LightweightCollection<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1, 2);

        simulateAddVisualItems(visualItems);

        ArgumentCaptor<VisualItem> captor = ArgumentCaptor
                .forClass(VisualItem.class);
        verify(arcType, times(2)).getArcs(captor.capture(),
                any(VisualItemContainer.class));
        assertThat(captor.getAllValues(),
                CollectionMatchers.containsExactly(visualItems.toList()));
    }

    @Test
    public void addResourceItemToAllResource() {
        ResourceSet resourceSet = ResourceSetTestUtils.createResources(1);
        VisualItem visualItem = VisualItemTestUtils.createVisualItem("1",
                resourceSet);

        init();

        simulateAddVisualItems(LightweightCollections.toCollection(visualItem));

        resourceSet.add(ResourceSetTestUtils.createResource(2));

        assertThat(underTest.getAllResources(), containsExactly(resourceSet));
    }

    private void addVisualItemToUnderTest(
            LightweightCollection<VisualItem> visualItems) {

        underTest.update(Delta.createAddedDelta(visualItems),
                LightweightCollections.<Slot> emptySet());
    }

    @Test
    public void arcsAreAddedWhenAddingResourceItems() {
        String arcId = "arcid";
        LightweightList<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1, 2);

        arcStyleProviderReturnArcType();
        init();

        arcTypeReturnsArcs(eq(visualItems.get(0)), createArc(arcId, 1, 2));
        arcTypeReturnsArcs(eq(visualItems.get(1)));

        simulateAddVisualItems(visualItems);

        verifyArcShown(arcId, "1", "2");
    }

    @Test
    public void arcsAreRemovedWhenSettingArcTypeNotInvisible() {
        String arcId = "arcid";
        LightweightList<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1, 2);

        arcStyleProviderReturnArcType();
        init();

        arcTypeReturnsArcs(eq(visualItems.get(0)), createArc(arcId, 1, 2));
        arcTypeReturnsArcs(eq(visualItems.get(1)));

        simulateAddVisualItems(visualItems);
        when(graphDisplay.containsArc(arcId)).thenReturn(true);

        underTest.setArcTypeVisible(arcType.getArcTypeID(), false);

        verifyArcRemoved(arcId, "1", "2");
    }

    @Test
    public void arcsAreShownWhenContainerVisibleSetTrueAfterBeingCreatedWhileVisibleWasFalse() {
        arcStyleProviderReturnArcType();
        init();

        arcTypeReturnsArcs(any(VisualItem.class), createArc("arcid1", 1, 2));
        underTest.setArcTypeVisible(arcTypeId, false);
        simulateAddVisualItems(VisualItemTestUtils.createVisualItems(1, 2));
        underTest.setArcTypeVisible(arcTypeId, true);

        verifyArcShown("arcid1", 1, 2);
    }

    private void arcStyleProviderReturnArcType() {
        when(arcStyleProvider.getArcTypes()).thenReturn(
                LightweightCollections.toCollection(arcType));
    }

    private void arcTypeReturnsArcs(VisualItem visualItem, Arc... arcs) {
        when(arcType.getArcs(visualItem, any(VisualItemContainer.class)))
                .thenReturn(LightweightCollections.toCollection(arcs));
    }

    private Arc createArc(String arcId, int from, int to) {
        return createArc(arcId, "" + from, "" + to);
    }

    private Arc createArc(String arcId, String from, String to) {
        return new Arc(arcId, from, to, arcTypeId, arcLabel, arcDirected, null);
    }

    /*
     * Test case: node drag event gets fired, test that correct move command is
     * added to the command manager
     */
    @Test
    public void createNodeMoveCommandWhenNodeDragged() {
        init();

        ArgumentCaptor<NodeDragHandler> argument1 = ArgumentCaptor
                .forClass(NodeDragHandler.class);

        verify(graphDisplay, times(1)).addEventHandler(eq(NodeDragEvent.TYPE),
                argument1.capture());

        NodeDragHandler nodeDragHandler = argument1.getValue();

        NodeDragEvent event = new NodeDragEvent(node, chooselEvent,
                sourceLocation.getX(), sourceLocation.getY(),
                targetLocation.getX(), targetLocation.getY());

        // when(underTest.getVisualItem(node)).thenReturn(visualItem);
        // when(chooselEvent.getBrowserEvent()).thenReturn(browserEvent);

        try {
            nodeDragHandler.onDrag(event);
        } catch (NullPointerException e) {
            // Do nothing; cannot mock deep enough here, resulting in this.
        }

        ArgumentCaptor<UndoableCommand> argument2 = ArgumentCaptor
                .forClass(UndoableCommand.class);

        verify(commandManager, times(1)).execute(argument2.capture());

        UndoableCommand command = argument2.getValue();
        assertThat(command.hasExecuted(), is(true));
        assertEquals(true, command instanceof MoveNodeCommand);

        MoveNodeCommand command2 = (MoveNodeCommand) command;

        assertEquals(node, command2.getNode());
        assertEquals(sourceLocation, command2.getSourceLocation());
        assertEquals(targetLocation, command2.getTargetLocation());
        assertEquals(graphDisplay, command2.getGraphDisplay());
    }

    @Test
    public void doNotShowArcItemOnCreationIfContainerVisibleSetFalse() {
        arcStyleProviderReturnArcType();
        init();

        arcTypeReturnsArcs(any(VisualItem.class), createArc("arcid1", 1, 2));
        underTest.setArcTypeVisible(arcTypeId, false);
        simulateAddVisualItems(VisualItemTestUtils.createVisualItems(1, 2));

        verifyNoArcAdded();
    }

    @Test
    public void doNotShowArcItemsThatRequireUnknownVisualItems() {
        arcStyleProviderReturnArcType();
        init();

        arcTypeReturnsArcs(any(VisualItem.class), createArc("arcid1", 1, 2),
                createArc("arcid2", 2, 1));
        simulateAddVisualItems(VisualItemTestUtils.createVisualItems(1));

        verifyNoArcAdded();
    }

    @Test
    public void getAllNodes() {
        init();

        VisualItem visualItem1 = VisualItemTestUtils.createVisualItem(1);
        VisualItem visualItem2 = VisualItemTestUtils.createVisualItem(2);

        simulateAddVisualItems(LightweightCollections.toCollection(visualItem1,
                visualItem2));

        Node node1 = ((NodeItem) visualItem1.getDisplayObject()).getNode();
        Node node2 = ((NodeItem) visualItem2.getDisplayObject()).getNode();

        assertThat(underTest.getAllNodes(),
                CollectionMatchers.containsExactly(CollectionUtils.toList(
                        node1, node2)));
    }

    private void init() {
        underTest = new Graph(graphDisplay, commandManager, resourceManager,
                resourceCategorizer, arcStyleProvider, registry, errorHandler,
                dataTypeValidator);
        underTest.init(visualItemContainer, callback);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadNeighbourhoodWhenAddingConcept() {
        init();

        Resource resource = ResourceSetTestUtils.createResource(1);
        VisualItem visualItem = VisualItemTestUtils.createVisualItem("1",
                ResourceSetTestUtils.toResourceSet(resource));

        stubColorSlotValues(visualItem);
        callback.addVisualItem(visualItem);
        addVisualItemToUnderTest(LightweightCollections
                .toCollection(visualItem));

        ArgumentCaptor<DefaultVisualItem> argument = ArgumentCaptor
                .forClass(DefaultVisualItem.class);
        verify(automaticExpander, times(1)).expand(argument.capture(),
                any(NodeExpansionCallback.class));

        VisualItem result = argument.getValue();
        assertEquals(1, result.getResources().size());
        assertEquals(resource, result.getResources().getFirstElement());
    }

    @Test
    public void removeResourceItemFromAllResource() {
        init();

        ResourceSet resourceSet = ResourceSetTestUtils.createResources(1);
        VisualItem visualItem = VisualItemTestUtils.createVisualItem("1",
                resourceSet);

        stubColorSlotValues(visualItem);
        callback.addVisualItem(visualItem);
        addVisualItemToUnderTest(LightweightCollections
                .toCollection(visualItem));

        underTest.update(Delta.createRemovedDelta(LightweightCollections
                .toCollection(visualItem)), LightweightCollections
                .<Slot> emptyCollection());

        assertThat(underTest.getAllResources(),
                containsExactly(ResourceSetTestUtils.createResources()));

    }

    @Test
    public void removeSourceResourceItemRemovesArc() {
        String arcId = "arcid";
        String groupId1 = "1";
        String groupId2 = "2";

        ResourceSet resourceSet1 = ResourceSetTestUtils.createResources(1);
        ResourceSet resourceSet2 = ResourceSetTestUtils.createResources(2);

        VisualItem resourceItem1 = VisualItemTestUtils.createVisualItem(
                groupId1, resourceSet1);
        VisualItem resourceItem2 = VisualItemTestUtils.createVisualItem(
                groupId2, resourceSet2);

        LightweightCollection<VisualItem> resourceItems = LightweightCollections
                .toCollection(resourceItem1, resourceItem2);

        arcStyleProviderReturnArcType();
        init();
        Arc arc = createArc(arcId, groupId1, groupId2);
        arcTypeReturnsArcs(eq(resourceItem1), arc);
        arcTypeReturnsArcs(eq(resourceItem2), arc);

        // simulate add
        // when(graphDisplay.containsNode(groupId1)).thenReturn(true);
        // when(graphDisplay.containsNode(groupId2)).thenReturn(true);
        // callback.addResourceItems(resourceItems);
        // addResourceItemToUnderTest(resourceItems);
        simulateAddVisualItems(resourceItems);
        when(graphDisplay.containsArc(arcId)).thenReturn(true);

        // simulate remove
        when(graphDisplay.containsNode(groupId1)).thenReturn(false);
        callback.removeResourceItem(resourceItem1);
        underTest.update(Delta.createRemovedDelta(LightweightCollections
                .toCollection(resourceItem1)), LightweightCollections
                .<Slot> emptyCollection());

        verifyArcRemoved(arcId, groupId1, groupId2);
    }

    @Test
    public void removeTargetResourceItemRemovesArc() {
        String arcId = "arcid";
        String groupId1 = "1";
        String groupId2 = "2";

        ResourceSet resourceSet1 = ResourceSetTestUtils.createResources(1);
        ResourceSet resourceSet2 = ResourceSetTestUtils.createResources(2);

        VisualItem visualItem1 = VisualItemTestUtils.createVisualItem(groupId1,
                resourceSet1);
        VisualItem visualItem2 = VisualItemTestUtils.createVisualItem(groupId2,
                resourceSet2);

        LightweightCollection<VisualItem> visualItems = LightweightCollections
                .toCollection(visualItem1, visualItem2);

        arcStyleProviderReturnArcType();
        init();
        Arc arc = createArc(arcId, groupId1, groupId2);
        arcTypeReturnsArcs(eq(visualItem1), arc);
        arcTypeReturnsArcs(eq(visualItem2), arc);

        // simulate add
        simulateAddVisualItems(visualItems);
        when(graphDisplay.containsArc(arcId)).thenReturn(true);

        // simulate remove
        when(graphDisplay.containsNode(groupId2)).thenReturn(false);
        callback.removeResourceItem(visualItem2);
        underTest.update(Delta.createRemovedDelta(LightweightCollections
                .toCollection(visualItem2)), LightweightCollections
                .<Slot> emptyCollection());

        verifyArcRemoved(arcId, groupId1, groupId2);
    }

    @Test
    public void setArcColorOnContainerChangesColorOfExistingArcs() {
        LightweightList<VisualItem> resourceItems = VisualItemTestUtils
                .createVisualItems(1, 2);

        arcStyleProviderReturnArcType();
        init();

        Arc arc = createArc("arcid", 1, 2);
        arcTypeReturnsArcs(eq(resourceItems.get(0)), arc);
        arcTypeReturnsArcs(eq(resourceItems.get(1)));

        simulateAddVisualItems(resourceItems);

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_COLOR), eq(arcColor.toHex()));

        Color newColor = new Color("#ff0000");
        underTest.getArcItemContainer(arcTypeId).setArcColor(newColor.toHex());

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_COLOR), eq(newColor.toHex()));
    }

    @Test
    public void setArcColorOnContainerChangesColorOfNewArcs() {
        String arcId = "arcid";
        String groupId1 = "1";
        String groupId2 = "2";
        LightweightList<VisualItem> resourceItems = VisualItemTestUtils
                .createVisualItems(1, 2);

        arcStyleProviderReturnArcType();
        init();

        String newColor = "#ff0000";
        underTest.getArcItemContainer(arcTypeId).setArcColor(newColor);

        Arc arc = createArc(arcId, groupId1, groupId2);
        arcTypeReturnsArcs(eq(resourceItems.get(0)), arc);
        arcTypeReturnsArcs(eq(resourceItems.get(1)));

        simulateAddVisualItems(resourceItems);

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_COLOR), eq(newColor));
    }

    @Test
    public void setArcStyleOnContainerChangesStyleOfExistingArcs() {
        arcStyleProviderReturnArcType();
        init();

        LightweightList<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1, 2);
        Arc arc = createArc("arcid", 1, 2);
        arcTypeReturnsArcs(eq(visualItems.get(0)), arc);
        arcTypeReturnsArcs(eq(visualItems.get(1)));

        simulateAddVisualItems(visualItems);

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_STYLE), eq(arcStyle));

        String newStyle = ArcSettings.ARC_STYLE_DASHED;
        underTest.getArcItemContainer(arcTypeId).setArcStyle(newStyle);

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_STYLE), eq(newStyle));
    }

    @Test
    public void setArcStyleOnContainerChangesStyleOfNewArcs() {
        arcStyleProviderReturnArcType();
        init();

        LightweightList<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1, 2);
        String newStyle = ArcSettings.ARC_STYLE_DASHED;
        underTest.getArcItemContainer(arcTypeId).setArcStyle(newStyle);

        Arc arc = createArc("arcid", 1, 2);
        arcTypeReturnsArcs(eq(visualItems.get(0)), arc);
        arcTypeReturnsArcs(eq(visualItems.get(1)));

        simulateAddVisualItems(visualItems);

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_STYLE), eq(newStyle));
    }

    @Test
    public void setArcHeadOnContainerChangesStyleOfExistingArcs() {
        arcStyleProviderReturnArcType();
        init();

        LightweightList<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1, 2);
        Arc arc = createArc("arcid", 1, 2);
        arcTypeReturnsArcs(eq(visualItems.get(0)), arc);
        arcTypeReturnsArcs(eq(visualItems.get(1)));

        simulateAddVisualItems(visualItems);

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_HEAD), eq(arcHead));

        String newHead = ArcSettings.ARC_HEAD_TRIANGLE_FULL;
        underTest.getArcItemContainer(arcTypeId).setArcStyle(newHead);

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_HEAD), eq(newHead));
    }

    @Test
    public void setArcHeadOnContainerChangesStyleOfNewArcs() {
        arcStyleProviderReturnArcType();
        init();

        LightweightList<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1, 2);
        String newStyle = ArcSettings.ARC_HEAD_TRIANGLE_FULL;
        underTest.getArcItemContainer(arcTypeId).setArcStyle(newStyle);

        Arc arc = createArc("arcid", 1, 2);
        arcTypeReturnsArcs(eq(visualItems.get(0)), arc);
        arcTypeReturnsArcs(eq(visualItems.get(1)));

        simulateAddVisualItems(visualItems);

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_HEAD), eq(newStyle));
    }

    @Test
    public void setArcThicknessOnContainerChangesThicknessOfExistingArcs() {
        arcStyleProviderReturnArcType();
        init();

        LightweightList<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1, 2);
        Arc arc = createArc("arcid", 1, 2);
        arcTypeReturnsArcs(eq(visualItems.get(0)), arc);
        arcTypeReturnsArcs(eq(visualItems.get(1)));

        simulateAddVisualItems(visualItems);

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_THICKNESS), eq("" + arcThickness));

        int newThickness = 4;
        underTest.getArcItemContainer(arcTypeId).setArcThicknessLevel(
                newThickness);

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_THICKNESS), eq("" + newThickness));
    }

    @Test
    public void setArcThicknessOnContainerChangesThicknessOfNewArcs() {
        arcStyleProviderReturnArcType();
        init();

        LightweightList<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1, 2);
        int newThickness = 4;
        underTest.getArcItemContainer(arcTypeId).setArcThicknessLevel(
                newThickness);

        Arc arc = createArc("arcid", 1, 2);
        arcTypeReturnsArcs(eq(visualItems.get(0)), arc);
        arcTypeReturnsArcs(eq(visualItems.get(1)));

        simulateAddVisualItems(visualItems);

        verify(graphDisplay, times(1)).setArcStyle(eq(arc),
                eq(ArcSettings.ARC_THICKNESS), eq("" + newThickness));
    }

    @Before
    public void setUp() throws Exception {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        callback = spy(new TestViewContentDisplayCallback());
        // TODO split, use separate classes
        visualItemContainer = callback;

        sourceLocation = new Point(10, 15);
        targetLocation = new Point(20, 25);

        arcTypeId = "arcType";
        arcLabel = "arcLabel";
        arcDirected = true;
        arcColor = new Color("#ffffff");
        arcThickness = 1;
        arcStyle = ArcSettings.ARC_STYLE_SOLID;
        arcHead = ArcSettings.ARC_HEAD_TRIANGLE_FULL;

        borderColor = new Color("#ff0000");
        backgroundColor = new Color("#ff0000");

        when(arcStyleProvider.getArcTypes()).thenReturn(
                LightweightCollections.<ArcType> emptyCollection());

        when(arcType.getArcTypeID()).thenReturn(arcTypeId);
        when(arcType.getArcTypeLabel()).thenReturn(arcLabel);
        when(arcType.getDefaultArcColor()).thenReturn(arcColor.toHex());
        when(arcType.getDefaultArcStyle()).thenReturn(arcStyle);
        when(arcType.getDefaultArcThickness()).thenReturn(arcThickness);
        when(arcType.getDefaultArcHead()).thenReturn(arcHead);

        final ArgumentCaptor<Integer> captor = ArgumentCaptor
                .forClass(Integer.class);
        // Mocks making me re-implement things for testing. This is not a good
        // result of having tests.
        Answer<Integer> answer = new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return (0 == captor.getValue()) ? arcType
                        .getDefaultArcThickness() : captor.getValue();
            }
        };
        when(arcType.getArcThickness(any(Arc.class), captor.capture()))
                .thenAnswer(answer);// thenReturn(captor.getValue());

        when(resourceCategorizer.getCategory(any(Resource.class))).thenReturn(
                ResourceSetTestUtils.TYPE_1);

        when(registry.getAutomaticExpander(any(String.class))).thenReturn(
                automaticExpander);

        when(registry.getAutomaticBulkExpander(any(String.class))).thenReturn(
                automaticBulkExpander);

        when(node.getId()).thenReturn("node1");
    }

    private void simulateAddVisualItems(
            LightweightCollection<VisualItem> visualItems) {
        for (VisualItem visualItem : visualItems) {
            when(graphDisplay.containsNode(visualItem.getId()))
                    .thenReturn(true);
            stubColorSlotValues(visualItem);
        }
        callback.addVisualItems(visualItems);
        addVisualItemToUnderTest(visualItems);
    }

    public void stubColorSlotValues(VisualItem visualItem) {
        when(visualItem.getValue(Graph.NODE_BORDER_COLOR)).thenReturn(
                borderColor);
        when(visualItem.getValue(Graph.NODE_BACKGROUND_COLOR)).thenReturn(
                backgroundColor);
    }

    @After
    public void tearDown() {
        MockitoGWTBridge.tearDown();
    }

    @Test
    public void updateArcsForResourceItems() {
        LightweightList<VisualItem> visualItems = VisualItemTestUtils
                .createVisualItems(1, 2);

        arcStyleProviderReturnArcType();
        init();
        arcTypeReturnsArcs(any(VisualItem.class));
        simulateAddVisualItems(visualItems);

        arcTypeReturnsArcs(eq(visualItems.get(0)), createArc("arcid", 1, 2));

        underTest.updateArcsForVisuaItems(visualItems);

        verifyArcShown("arcid", "1", "2");

    }

    private void verifyArcRemoved(String arcId, String sourceNodeId,
            String targetNodeId) {

        ArgumentCaptor<Arc> captor = ArgumentCaptor.forClass(Arc.class);
        verify(graphDisplay, times(1)).removeArc(captor.capture());
        Arc result = captor.getValue();
        assertEquals(arcId, result.getId());
        assertEquals(sourceNodeId, result.getSourceNodeId());
        assertEquals(targetNodeId, result.getTargetNodeId());
        assertEquals(arcTypeId, result.getType());
        assertEquals(arcLabel, result.getLabel());
    }

    private void verifyArcShown(String arcId, int sourceNodeId, int targetNodeId) {
        verifyArcShown(arcId, "" + sourceNodeId, "" + targetNodeId);
    }

    private void verifyArcShown(String arcId, String sourceNodeId,
            String targetNodeId) {

        ArgumentCaptor<Arc> captor = ArgumentCaptor.forClass(Arc.class);
        verify(graphDisplay, times(1)).addArc(captor.capture());
        Arc result = captor.getValue();
        assertEquals(arcId, result.getId());
        assertEquals(sourceNodeId, result.getSourceNodeId());
        assertEquals(targetNodeId, result.getTargetNodeId());
        assertEquals(arcTypeId, result.getType());
        assertEquals(arcLabel, result.getLabel());
    }

    private void verifyNoArcAdded() {
        verify(graphDisplay, times(0)).addArc(any(Arc.class));
    }

}
