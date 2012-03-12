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
package org.thechiselgroup.biomixer.client;

import static org.thechiselgroup.biomixer.client.BioMixerVisualItemValueResolverFactoryProvider.CONCEPT_BY_ONTOLOGY_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.BioMixerVisualItemValueResolverFactoryProvider.GRAPH_LABEL_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.BioMixerVisualItemValueResolverFactoryProvider.NODE_BACKGROUND_COLOR_RESOLVER_FACTORY;
import static org.thechiselgroup.biomixer.client.BioMixerVisualItemValueResolverFactoryProvider.NODE_BORDER_COLOR_RESOLVER_FACTORY;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.thechiselgroup.biomixer.client.core.resources.HasResourceCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.ui.SidePanelSection;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.util.predicates.Predicate;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualizationModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.DefaultManagedSlotMappingConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.PresetSlotMappingInitializer;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.SlotMappingInitializer;
import org.thechiselgroup.biomixer.client.core.visualization.ui.NullVisualMappingsControl;
import org.thechiselgroup.biomixer.client.core.visualization.ui.VisualMappingsControl;
import org.thechiselgroup.biomixer.client.dnd.resources.DropEnabledViewContentDisplay;
import org.thechiselgroup.biomixer.client.graph.ConceptArcType;
import org.thechiselgroup.biomixer.client.graph.DirectConceptMappingArcType;
import org.thechiselgroup.biomixer.client.graph.MappingArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ArcItemContainer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.ArcSettings;
import org.thechiselgroup.biomixer.client.workbench.ChooselWorkbenchViewWindowContentProducer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class BioMixerViewWindowContentProducer extends
        ChooselWorkbenchViewWindowContentProducer {

    public static class IsNotAMappingPredicate implements Predicate<Resource> {
        @Override
        public boolean evaluate(Resource value) {
            return !Mapping.isMapping(value);
        }
    }

    private SidePanelSection createArcsSidePanelSection(
            ViewContentDisplay contentDisplay) {

        assert ((DropEnabledViewContentDisplay) contentDisplay).getDelegate() instanceof Graph : "invalid content display type "
                + contentDisplay;

        Graph graphViewContentDisplay = (Graph) ((DropEnabledViewContentDisplay) contentDisplay)
                .getDelegate();

        VerticalPanel panel = new VerticalPanel();
        Iterable<ArcItemContainer> arcItemContainers = graphViewContentDisplay
                .getArcItemContainers();
        for (final ArcItemContainer arcItemContainer : arcItemContainers) {
            String arcTypeId = arcItemContainer.getArcType().getArcTypeID();
            String label = "unknown";
            if (DirectConceptMappingArcType.ID.equals(arcTypeId)) {
                label = "Concept Mapping";
            } else if (MappingArcType.ID.equals(arcTypeId)) {
                label = "Mapping";
            } else if (ConceptArcType.ID.equals(arcTypeId)) {
                label = "Concept Relationship";
            }

            panel.add(createArcTypeContainerControl(label, arcItemContainer));
        }

        return new SidePanelSection("Arcs", panel);
    }

    // TODO extract & move
    private VerticalPanel createArcTypeContainerControl(String label,
            final ArcItemContainer arcItemContainer) {

        final TextBox arcColorText = new TextBox();
        arcColorText.setText(arcItemContainer.getArcColor());

        final ListBox arcStyleDropDown = new ListBox();
        arcStyleDropDown.setVisibleItemCount(1);
        arcStyleDropDown.addItem(ArcSettings.ARC_STYLE_SOLID);
        arcStyleDropDown.addItem(ArcSettings.ARC_STYLE_DASHED);
        String arcStyle = arcItemContainer.getArcStyle();
        if (ArcSettings.ARC_STYLE_DASHED.equals(arcStyle)) {
            arcStyleDropDown.setSelectedIndex(1);
        } else {
            arcStyleDropDown.setSelectedIndex(0);
        }

        final ListBox arcThicknessDropDown = new ListBox();
        arcThicknessDropDown.setVisibleItemCount(1);
        arcThicknessDropDown.addItem("1");
        arcThicknessDropDown.addItem("2");
        arcThicknessDropDown.addItem("3");
        arcThicknessDropDown.addItem("4");
        arcThicknessDropDown.addItem("5");
        int arcThickness = arcItemContainer.getArcThickness();
        arcThicknessDropDown.setSelectedIndex(arcThickness - 1);

        final Button updateButton = new Button("Update Arcs");
        updateButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                arcItemContainer.setArcColor(arcColorText.getText());
                arcItemContainer.setArcStyle(arcStyleDropDown
                        .getItemText(arcStyleDropDown.getSelectedIndex()));
                arcItemContainer.setArcThickness(Integer
                        .parseInt(arcThicknessDropDown
                                .getItemText(arcThicknessDropDown
                                        .getSelectedIndex())));
            }
        });

        final CheckBox visibleCheckBox = new CheckBox("Arcs Visible");
        visibleCheckBox.setValue(true);
        visibleCheckBox
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        boolean value = visibleCheckBox.getValue();
                        updateButton.setEnabled(value);
                        arcStyleDropDown.setEnabled(value);
                        arcThicknessDropDown.setEnabled(value);
                        arcColorText.setEnabled(value);
                        arcItemContainer.setVisible(value);
                    }

                });

        VerticalPanel containerPanel = new VerticalPanel();

        containerPanel.add(new Label(label));
        containerPanel.add(visibleCheckBox);
        containerPanel.add(new Label("Arc Color"));
        containerPanel.add(arcColorText);
        containerPanel.add(new Label("Arc Style"));
        containerPanel.add(arcStyleDropDown);
        containerPanel.add(new Label("Arc Thickness"));
        containerPanel.add(arcThicknessDropDown);
        containerPanel.add(updateButton);
        return containerPanel;
    }

    private SidePanelSection createNodesSidePanelSection(
            final ResourceModel resourceModel,
            final VisualizationModel visualizationModel) {

        final VerticalPanel panel = new VerticalPanel();

        final Map<String, CheckBox> ontologyToFilterBox = CollectionFactory
                .createStringMap();
        final CheckBox mappingNodesCheckbox = new CheckBox("Show Mapping Nodes");
        mappingNodesCheckbox.setValue(false);
        mappingNodesCheckbox
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        updatePredicate(resourceModel,
                                mappingNodesCheckbox.getValue(),
                                ontologyToFilterBox);
                    }
                });

        final CheckBox colorByOntologyCheckBox = new CheckBox(
                "Color Concept Nodes by Ontology");
        colorByOntologyCheckBox.setValue(true);
        colorByOntologyCheckBox
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        boolean value = colorByOntologyCheckBox.getValue();

                        VisualItemValueResolver resolver;
                        if (value) {
                            resolver = CONCEPT_BY_ONTOLOGY_RESOLVER_FACTORY
                                    .create();
                        } else {
                            resolver = NODE_BACKGROUND_COLOR_RESOLVER_FACTORY
                                    .create();
                        }

                        visualizationModel.setResolver(
                                Graph.NODE_BACKGROUND_COLOR, resolver);
                    }
                });

        resourceModel.getResources().addEventHandler(
                new ResourceSetChangedEventHandler() {
                    @Override
                    public void onResourceSetChanged(
                            ResourceSetChangedEvent event) {
                        LightweightCollection<Resource> addedResources = event
                                .getAddedResources();
                        for (Resource resource : addedResources) {
                            if (Concept.isConcept(resource)) {
                                String ontologyId = (String) resource
                                        .getValue(Concept.VIRTUAL_ONTOLOGY_ID);
                                if (!ontologyToFilterBox
                                        .containsKey(ontologyId)) {

                                    CheckBox checkBox = new CheckBox(
                                            "<span style='color: "
                                                    + BioMixerConceptByOntologyColorResolver
                                                            .getColor(ontologyId)
                                                    + "'>&#9609;</span>"
                                                    + "&nbsp;"
                                                    + "Show "
                                                    + resource
                                                            .getValue(Concept.CONCEPT_ONTOLOGY_NAME),
                                            true);
                                    checkBox.setValue(true);
                                    ontologyToFilterBox.put(ontologyId,
                                            checkBox);
                                    panel.add(checkBox);
                                    checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                                        @Override
                                        public void onValueChange(
                                                ValueChangeEvent<Boolean> event) {
                                            updatePredicate(resourceModel,
                                                    mappingNodesCheckbox
                                                            .getValue(),
                                                    ontologyToFilterBox);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });

        panel.add(mappingNodesCheckbox);
        panel.add(colorByOntologyCheckBox);

        updatePredicate(resourceModel, false, ontologyToFilterBox);

        return new SidePanelSection("Nodes", panel);
    }

    // TODO FOR GRAPH only, create special parts
    @Override
    protected LightweightList<SidePanelSection> createSidePanelSections(
            String contentType, ViewContentDisplay contentDisplay,
            VisualMappingsControl visualMappingsControl,
            ResourceModel resourceModel, VisualizationModel visualizationModel) {

        if (Graph.ID.equals(contentType)) {
            assert contentDisplay != null;

            LightweightList<SidePanelSection> sidePanelSections = CollectionFactory
                    .createLightweightList();

            sidePanelSections.add(createNodesSidePanelSection(resourceModel,
                    visualizationModel));
            sidePanelSections.add(createArcsSidePanelSection(contentDisplay));
            // sidePanelSections.addAll(contentDisplay.getSidePanelSections());
            {
                TextArea textArea = new TextArea();
                textArea.setWidth("100%");
                textArea.setHeight("100%");

                sidePanelSections
                        .add(new SidePanelSection("Comments", textArea));
            }

            return sidePanelSections;
        }

        return super.createSidePanelSections(contentType, contentDisplay,
                visualMappingsControl, resourceModel, visualizationModel);
    }

    @Override
    protected SlotMappingInitializer createSlotMappingInitializer(
            String contentType) {

        if (Graph.ID.equals(contentType)) {
            PresetSlotMappingInitializer initializer = new PresetSlotMappingInitializer();

            initializer.putSlotMapping(Graph.NODE_LABEL_SLOT,
                    GRAPH_LABEL_RESOLVER_FACTORY);
            initializer.putSlotMapping(Graph.NODE_BACKGROUND_COLOR,
                    CONCEPT_BY_ONTOLOGY_RESOLVER_FACTORY);
            initializer.putSlotMapping(Graph.NODE_BORDER_COLOR,
                    NODE_BORDER_COLOR_RESOLVER_FACTORY);

            return initializer;
        }

        return super.createSlotMappingInitializer(contentType);
    }

    @Override
    protected VisualMappingsControl createVisualMappingsControl(
            HasResourceCategorizer resourceGrouping,
            DefaultManagedSlotMappingConfiguration uiModel, String contentType) {

        if (Graph.ID.equals(contentType)) {
            return new NullVisualMappingsControl();
        }

        return super.createVisualMappingsControl(resourceGrouping, uiModel,
                contentType);
    }

    private void updatePredicate(ResourceModel resourceModel,
            final boolean showMappings,
            Map<String, CheckBox> ontologyToFilterBox) {

        final Map<String, Boolean> values = CollectionFactory.createStringMap();
        Set<Entry<String, CheckBox>> set = ontologyToFilterBox.entrySet();
        for (Entry<String, CheckBox> entry : set) {
            values.put(entry.getKey(), entry.getValue().getValue());
        }

        resourceModel.setFilterPredicate(new BioMixerGraphFilterPredicate(
                values, showMappings));
    }
}
