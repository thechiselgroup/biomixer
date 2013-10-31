/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel, Bo Fu 
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

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ui.AbstractDetailsWidgetHelper;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.biomixer.client.core.util.url.BioportalWebUrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilder;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.dnd.resources.DraggableResourceSetAvatar;
import org.thechiselgroup.biomixer.client.dnd.resources.ResourceSetAvatarDragController;
import org.thechiselgroup.biomixer.client.utils.HtmlDecoder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BioMixerDetailsWidgetHelper extends
        AbstractDetailsWidgetHelper<VisualItem> {

    private final ResourceSetAvatarDragController dragController;

    private final ResourceManager resourceManager;

    /**
     * Hack to fully decode strings containing HTML encoded characters. Some
     * symbols (e.g. |) were showing up in the LOINC ontology. JQuery decodes
     * things well, so we use an element to do the dirty work.
     */

    // &#8632; //&#10138; //&#10149; //&#10548;
    static String ARROW_FOR_OUTLINK = " " + HtmlDecoder.decode("&#10138;");

    @Inject
    public BioMixerDetailsWidgetHelper(
            ResourceSetAvatarFactory dragAvatarFactory,
            ResourceSetAvatarDragController dragController,
            ResourceManager resourceManager) {

        super(dragAvatarFactory);

        this.dragController = dragController;
        this.resourceManager = resourceManager;
    }

    protected ResourceSetAvatar createAvatar(String label,
            ResourceSet resourceSet) {
        ResourceSetAvatar avatar = new DraggableResourceSetAvatar(label
                + ARROW_FOR_OUTLINK, "avatar-resourceSet", resourceSet,
                ResourceSetAvatarType.SET);
        avatar.setEnabled(true);
        dragController.setDraggable(avatar, true);
        return avatar;
    }

    static public class VisualItemVerticalPanel extends VerticalPanel {
        private VisualItem visualItem;

        public VisualItemVerticalPanel() {
            // Zero arg for deferred binding, as set up before when we used
            // VerticalPanel directly for popups.
            super();
        }

        public void setVisualItem(VisualItem visualItem) {
            this.visualItem = visualItem;
        }

        public VisualItem getVisualItem() {
            return this.visualItem;
        }
    }

    // TODO use dragAvatarFactory (injection)
    @Override
    public Widget createDetailsWidget(VisualItem visualItem) {
        // XXX Used to use deferred binding via GWT.create(), but had trouble
        // when extending the VerticalPanel. Could extend Composite from GWT ui
        // package, but using "new" seems to work fine...do we really need to
        // use create() to begin with?
        // VisualItemVerticalPanel verticalPanel =
        // GWT.create(VisualItemVerticalPanel.class);
        VisualItemVerticalPanel verticalPanel = new VisualItemVerticalPanel();
        verticalPanel.setVisualItem(visualItem);
        return refreshDetailsWidget(visualItem, verticalPanel);
    }

    // TODO use dragAvatarFactory (injection)
    @Override
    public Widget refreshDetailsWidget(VisualItem visualItem,
            Widget existingWidget) {
        VerticalPanel verticalPanel = (VerticalPanel) existingWidget;
        ResourceSet resourceSet = visualItem.getResources();
        final Resource resource = resourceSet.getFirstElement();
        verticalPanel.clear();

        // FIXME use generic way to put in custom widgets
        if (Concept.isConcept(resource)) {
            // making the concept label clickable
            ResourceSetAvatar avatar = createAvatar(
                    (String) resource.getValue(Concept.LABEL), resourceSet);
            avatar.addClickHandler(new com.google.gwt.event.dom.client.ClickHandler() {
                @Override
                public void onClick(com.google.gwt.event.dom.client.ClickEvent e) {
                    com.google.gwt.user.client.Window.open(
                            (String) resource.getValue(Concept.ID), "_blank",
                            "");
                }
            });
            verticalPanel.add(avatar);

            // addRow(resource, verticalPanel, "Ontology",
            // Concept.CONCEPT_ONTOLOGY_NAME);
            addRow(resource, verticalPanel, "Concept ID", Concept.ID);
            addRow(resource, verticalPanel, "Ontology Acronym",
                    Concept.ONTOLOGY_ACRONYM);
            final UrlBuilder ontologySummaryUrl = BioportalWebUrlBuilder
                    .generateOntologySummaryUrl((String) resource
                            .getValue(Concept.ONTOLOGY_ACRONYM));
            addRow("Ontology Homepage", ontologySummaryUrl, verticalPanel, true);

        } else if (Ontology.isOntology(resource)) {
            // making the concept label clickable
            ResourceSetAvatar avatar = createAvatar(
                    (String) resource.getValue(Ontology.ONTOLOGY_FULL_NAME),
                    resourceSet);
            final UrlBuilder ontologySummaryUrl = BioportalWebUrlBuilder
                    .generateOntologySummaryUrl((String) resource
                            .getValue(Ontology.ONTOLOGY_ACRONYM));
            ClickHandler urlClickHandler = new ClickHandler() {
                @Override
                public void onClick(com.google.gwt.event.dom.client.ClickEvent e) {

                    com.google.gwt.user.client.Window.open(
                            ontologySummaryUrl.toString(), "_blank", "");
                }
            };
            avatar.addClickHandler(urlClickHandler);
            verticalPanel.add(avatar);

            // The summary url is also clickable. Perhaps they can have
            // different targets? Not sure...
            addRow("Summary", ontologySummaryUrl, verticalPanel, true);
            addRow(resource, verticalPanel, "Ontology Acronym",
                    Ontology.ONTOLOGY_ACRONYM);
            addRow(resource, verticalPanel, "Description",
                    Ontology.DESCRIPTION, false);
            addRow(resource, verticalPanel, "Num Classes",
                    Ontology.NUMBER_OF_CLASSES);
            addRow(resource, verticalPanel, "Num Individuals",
                    Ontology.NUMBER_OF_INDIVIDUALS);
            addRow(resource, verticalPanel, "Num Properties",
                    Ontology.NUMBER_OF_PROPERTIES);
            // Decided to remove Note, for now at least.
            // addRow(resource, verticalPanel, "Note", Ontology.NOTE);
            // addRow(resource, verticalPanel, "Access",
            // Ontology.VIEWING_RESTRICTIONS);

        } else if (Mapping.isMapping(resource)) {
            verticalPanel.add(createAvatar("Mapping", resourceSet));

            // addRow(resource, verticalPanel, "Created", Mapping.DATE);
            // addRow(resource, verticalPanel, "Mapping source",
            // Mapping.MAPPING_SOURCE);
            // addRow(resource, verticalPanel, "Mapping source name",
            // Mapping.MAPPING_SOURCE_NAME);
            // addRow(resource, verticalPanel, "Mapping type",
            // Mapping.MAPPING_TYPE);

            Resource sourceConcept = resourceManager.getByUri((String) resource
                    .getValue(Mapping.SOURCE_CONCEPT_URI));
            if (sourceConcept != null) {
                addRow(sourceConcept, verticalPanel, "Source concept",
                        Concept.LABEL);
                addRow(sourceConcept, verticalPanel, "Source ontology acronym",
                        Concept.ONTOLOGY_ACRONYM);
                // TODO ontology names (might need service for ontologies)
            }

            Resource targetConcept = resourceManager.getByUri((String) resource
                    .getValue(Mapping.TARGET_CONCEPT_URI));
            if (targetConcept != null) {
                addRow(targetConcept, verticalPanel, "Target concept",
                        Concept.LABEL);
                addRow(targetConcept, verticalPanel, "Target ontology acronym",
                        Concept.ONTOLOGY_ACRONYM);
                // TODO ontology names (might need service for ontologies)
            }
        } else {
            verticalPanel.add(avatarFactory.createAvatar(resourceSet));

            String value = "";
            HTML html = GWT.create(HTML.class);
            html.setHTML(value);
            verticalPanel.add(html);
        }

        return verticalPanel;
    }
}