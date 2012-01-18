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
package org.thechiselgroup.biomixer.client.core.resources.ui;

import java.util.Set;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * {@link AbstractDetailsWidgetHelper} that shows information from the view item
 * as well as from the underlying resources.
 * 
 * @author Lars Grammel
 */
public class DefaultDetailsWidgetHelper extends AbstractDetailsWidgetHelper {

    @Inject
    public DefaultDetailsWidgetHelper(ResourceSetFactory resourceSetFactory,
            ResourceSetAvatarFactory dragAvatarFactory) {
        super(resourceSetFactory, dragAvatarFactory);
    }

    @Override
    public Widget createDetailsWidget(VisualItem visualItem) {
        VerticalPanel verticalPanel = GWT.create(VerticalPanel.class);
        ResourceSetAvatar avatar = avatarFactory.createAvatar(visualItem
                .getResources());
        avatar.setText(visualItem.getId());
        verticalPanel.add(avatar);

        // try to resolve slot mappings first
        Slot[] slots = visualItem.getSlots();
        for (Slot slot : slots) {
            String label = slot.getName();
            Object valueObject = visualItem.getValue(slot);
            String value = valueObject != null ? valueObject.toString() : "";
            addRow(label, value, true, verticalPanel);
        }

        // single resource: show properties
        if (visualItem.getResources().size() == 1) {
            Resource resource = visualItem.getResources().getFirstElement();

            verticalPanel.add(new HTML("<br/><b>One item</b>"));
            Set<String> entrySet = resource.getProperties().keySet();
            for (String property : entrySet) {
                addRow(resource, verticalPanel, property, property);
            }

            return verticalPanel;
        }

        // multiple resources: show numbers
        verticalPanel.add(new HTML("<br/><b>" + visualItem.getResources().size()
                + " items</b>"));

        return verticalPanel;
    }
}
