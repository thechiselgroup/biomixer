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

import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ui.AbstractDetailsWidgetHelper;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.biomixer.client.dnd.resources.DraggableResourceSetAvatar;
import org.thechiselgroup.biomixer.client.graph.CompositionArcType;
import org.thechiselgroup.biomixer.client.graph.ConceptArcType;
import org.thechiselgroup.biomixer.client.graph.DirectConceptMappingArcType;
import org.thechiselgroup.biomixer.client.graph.MappingArcType;
import org.thechiselgroup.biomixer.client.graph.OntologyMappingArcType;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

// TODO Clean this up. Left commented out stuff as hints for future changes.
public class BioMixerDetailsWidgetRenderedArcHelper extends
        AbstractDetailsWidgetHelper<RenderedArc> {

    // private final ResourceSetAvatarDragController dragController;

    private final ResourceManager resourceManager;

    @Inject
    public BioMixerDetailsWidgetRenderedArcHelper(
    // ResourceSetFactory resourceSetFactory,
            ResourceSetAvatarFactory dragAvatarFactory,
            // ResourceSetAvatarDragController dragController,
            ResourceManager resourceManager) {

        super(
        // resourceSetFactory,
                dragAvatarFactory);

        // this.dragController = dragController;
        this.resourceManager = resourceManager;
    }

    protected ResourceSetAvatar createAvatar(String label,
            ResourceSet resourceSet) {
        ResourceSetAvatar avatar = new DraggableResourceSetAvatar(label,
                "avatar-resourceSet", resourceSet, ResourceSetAvatarType.SET);
        avatar.setEnabled(true);
        // dragController.setDraggable(avatar, true);
        return avatar;
    }

    public class RenderedArcVerticalPanel extends VerticalPanel {
        private RenderedArc renderedArc;

        public RenderedArcVerticalPanel() {
            // Zero arg for deferred binding, as set up before when we used
            // VerticalPanel directly for popups.
            super();
        }

        public void setRenderedArc(RenderedArc renderedArc) {
            this.renderedArc = renderedArc;
        }

        public RenderedArc getRenderedArc() {
            return this.renderedArc;
        }
    }

    // TODO use dragAvatarFactory (injection)
    @Override
    public Widget createDetailsWidget(RenderedArc arc) {
        // XXX Used to use deferred binding via GWT.create(), but had trouble
        // when extending the VerticalPanel. Could extend Composite from GWT ui
        // package, but using "new" seems to work fine...do we really need to
        // use create() to begin with?
        // VisualItemVerticalPanel verticalPanel =
        // GWT.create(VisualItemVerticalPanel.class);
        RenderedArcVerticalPanel verticalPanel = new RenderedArcVerticalPanel();
        verticalPanel.setRenderedArc(arc);
        return refreshDetailsWidget(arc, verticalPanel);
    }

    private String computedArcLabel(RenderedNode node,
            String parentheticalProperty) {
        return node.getNode().getLabel()
                + " ("
                + node.getNode().getVisualItem().getResources()
                        .getFirstElement().getValue(parentheticalProperty)
                + ")";
    }

    // TODO use dragAvatarFactory (injection)
    @Override
    public Widget refreshDetailsWidget(RenderedArc arc, Widget existingWidget) {
        VerticalPanel verticalPanel = (VerticalPanel) existingWidget;
        // ResourceSet resourceSet = arc.getResources();
        // final Resource resource = resourceSet.getFirstElement();
        verticalPanel.clear();

        // See BioMixerDetailsWidgetHelper for the node oriented version of
        // this.

        String type = arc.getArc().getType();

        // For now, simply make the thickness the popup contents.
        // Later when we have better ideas, change this conditional.
        if (type.equals(OntologyMappingArcType.ID)) {
            // See concept popup for clickable link avatars if desired
            // ResourceSetAvatar avatar = createAvatar(
            // (String) resource.getValue(Ontology.LABEL), resourceSet);
            // final UrlBuilder ontologySummaryUrl = BioportalWebUrlBuilder
            // .generateOntologySummaryUrl((String) resource
            // .getValue(Ontology.VIRTUAL_ONTOLOGY_ID));
            // ClickHandler urlClickHandler = new ClickHandler() {
            // @Override
            // public void onClick(com.google.gwt.event.dom.client.ClickEvent e)
            // {
            //
            // com.google.gwt.user.client.Window.open(
            // ontologySummaryUrl.toString(), "_blank", "");
            // }
            // };
            // avatar.addClickHandler(urlClickHandler);
            // verticalPanel.add(avatar);

            // The summary url is also clickable. Perhaps they can have
            // different targets? Not sure...
            String sourceLabel = computedArcLabel(arc.getSource(),
                    Ontology.ONTOLOGY_ACRONYM);
            String targetLabel = computedArcLabel(arc.getTarget(),
                    Ontology.ONTOLOGY_ACRONYM);

            addRow("Num of Mappings", arc.getArc().getSize() + "", true,
                    verticalPanel);
            addRow("From", sourceLabel, true, verticalPanel);
            addRow("To", targetLabel, true, verticalPanel);

            // When we get bidirectional rather than nondirectional mapping
            // arcs:
            // addRow("Num of Mappings", arc.getArc().getOutgoingSize() + "("
            // + sourceLabel + "->" + targetLabel + ")", true,
            // verticalPanel);
            // addRow("Num of Mappings", arc.getArc().getIncomingSize() + "("
            // + targetLabel + "->" + sourceLabel + ")", true,
            // verticalPanel);

        } else if (type.equals(MappingArcType.ID)) {
            String sourceLabel = computedArcLabel(arc.getSource(),
                    Concept.ONTOLOGY_ACRONYM);
            String targetLabel = computedArcLabel(arc.getTarget(),
                    Concept.ONTOLOGY_ACRONYM);
            addRow("From", sourceLabel, true, verticalPanel);
            addRow("To", targetLabel, true, verticalPanel);
        } else if (type.equals(ConceptArcType.ID)) {
            String sourceLabel = computedArcLabel(arc.getSource(),
                    Concept.ONTOLOGY_ACRONYM);
            String targetLabel = computedArcLabel(arc.getTarget(),
                    Concept.ONTOLOGY_ACRONYM);
            addRow("From", sourceLabel, true, verticalPanel);
            addRow("To", targetLabel, true, verticalPanel);
        } else if (type.equals(CompositionArcType.ID)) {
            String sourceLabel = computedArcLabel(arc.getSource(),
                    Concept.ONTOLOGY_ACRONYM);
            String targetLabel = computedArcLabel(arc.getTarget(),
                    Concept.ONTOLOGY_ACRONYM);
            addRow("From", sourceLabel, true, verticalPanel);
            addRow("To", targetLabel, true, verticalPanel);
        } else if (type.equals(DirectConceptMappingArcType.ID)) {
            String sourceLabel = computedArcLabel(arc.getSource(),
                    Concept.ONTOLOGY_ACRONYM);
            String targetLabel = computedArcLabel(arc.getTarget(),
                    Concept.ONTOLOGY_ACRONYM);
            addRow("From", sourceLabel, true, verticalPanel);
            addRow("To", targetLabel, true, verticalPanel);
        } else {
            // verticalPanel.add(avatarFactory.createAvatar(resourceSet));
            String value = "";
            HTML html = GWT.create(HTML.class);
            html.setHTML(value);
            verticalPanel.add(html);

        }

        return verticalPanel;
    }
}