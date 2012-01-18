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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ResourceSetAvatarResourceSetsPresenter implements
        ResourceSetsPresenter {

    public static final String CSS_PANEL = "ResourceSetAvatarResourceSetsPresenter-panel";

    // TODO move
    public static final String AVATAR_RESOURCE_SET = "avatar-resourceSet";

    // TODO move
    public static final String AVATAR_SELECTION = "avatar-selection";

    private ResourceSetAvatarFactory dragAvatarFactory;

    private HorizontalPanel panel;

    private Map<ResourceSet, ResourceSetAvatar> resourceSetsToDragAvatars = new HashMap<ResourceSet, ResourceSetAvatar>();

    @Inject
    public ResourceSetAvatarResourceSetsPresenter(
            ResourceSetAvatarFactory dragAvatarFactory) {

        assert dragAvatarFactory != null;
        this.dragAvatarFactory = dragAvatarFactory;
    }

    @Override
    public void addResourceSet(ResourceSet resources) {
        assert resources != null;
        assert panel != null;
        assert !resourceSetsToDragAvatars.containsKey(resources) : "Resource Set "
                + resources + " is already contained in this presenter";

        ResourceSetAvatar avatar = dragAvatarFactory.createAvatar(resources);

        panel.insert(avatar, resourceSetsToDragAvatars.size());

        resourceSetsToDragAvatars.put(resources, avatar);
    }

    @Override
    public Widget asWidget() {
        assert panel != null;
        return panel;
    }

    @Override
    public void dispose() {
        if (isDisposed()) {
            return;
        }

        for (ResourceSetAvatar avatar : new ArrayList<ResourceSetAvatar>(
                resourceSetsToDragAvatars.values())) {
            avatar.dispose();
        }

        resourceSetsToDragAvatars.clear();
        resourceSetsToDragAvatars = null;
        panel = null;
    }

    private ResourceSetAvatar getAvatar(ResourceSet resource) {
        return resourceSetsToDragAvatars.get(resource);
    }

    @Override
    public void init() {
        panel = GWT.create(HorizontalPanel.class);
        panel.setStyleName(CSS_PANEL);
        panel.setSpacing(2);
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    }

    private boolean isDisposed() {
        return resourceSetsToDragAvatars == null;
    }

    @Override
    public void removeResourceSet(ResourceSet resources) {
        assert panel != null;
        assert resourceSetsToDragAvatars.containsKey(resources);

        ResourceSetAvatar avatar = resourceSetsToDragAvatars.remove(resources);
        panel.remove(avatar); // needs to take place before dispose
        avatar.dispose();
    }

    @Override
    public void replaceResourceSet(ResourceSet oldResources,
            ResourceSet newResources) {

        assert resourceSetsToDragAvatars.containsKey(oldResources);
        assert !resourceSetsToDragAvatars.containsKey(newResources);

        ResourceSetAvatar avatar = resourceSetsToDragAvatars
                .remove(oldResources);

        resourceSetsToDragAvatars.put(newResources, avatar);

        avatar.setResourceSet(newResources);
        avatar.setText(newResources.getLabel());

        assert !resourceSetsToDragAvatars.containsKey(oldResources);
        assert resourceSetsToDragAvatars.containsKey(newResources);
    }

    @Override
    public void setResourceSetEnabled(ResourceSet resource, boolean enabled) {
        getAvatar(resource).setEnabled(enabled);
    }

    // TODO test
    @Override
    public void setSelectedResourceSet(ResourceSet selectedResourceSet) {
        for (ResourceSetAvatar avatar : resourceSetsToDragAvatars.values()) {
            // TODO should be flag on avatar
            if (avatar.getResourceSet().equals(selectedResourceSet)) {
                avatar.setEnabledCSSClass(AVATAR_SELECTION);
            } else {
                avatar.setEnabledCSSClass(AVATAR_RESOURCE_SET);
            }
        }
    }
}
