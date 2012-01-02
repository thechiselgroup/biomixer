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
package org.thechiselgroup.biomixer.client.core.visualization.ui;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetAddedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetAddedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetRemovedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetRemovedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetsPresenter;
import org.thechiselgroup.biomixer.client.core.ui.Presenter;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DefaultResourceModelPresenter implements Presenter {

    private ResourceSetsPresenter inputResourceSetsPresenter;

    private ResourceSetsPresenter allResourcesPresenter;

    private ResourceModel resourceModel;

    public DefaultResourceModelPresenter(
            ResourceSetsPresenter allResourcesPresenter,
            ResourceSetsPresenter inputResourceSetsPresenter,
            ResourceModel resourceModel) {

        this.allResourcesPresenter = allResourcesPresenter;
        this.inputResourceSetsPresenter = inputResourceSetsPresenter;
        this.resourceModel = resourceModel;
    }

    @Override
    public Widget asWidget() {
        HorizontalPanel widget = new HorizontalPanel();
        widget.add(allResourcesPresenter.asWidget());
        widget.add(inputResourceSetsPresenter.asWidget());
        return widget;
    }

    @Override
    public void dispose() {
        inputResourceSetsPresenter.dispose();
        inputResourceSetsPresenter = null;
        allResourcesPresenter.dispose();
        allResourcesPresenter = null;
    }

    @Override
    public void init() {
        initAllResourcePresenter();
        initInputResourceSetsPresenter();
    }

    private void initAllResourcePresenter() {
        allResourcesPresenter.init();
        allResourcesPresenter.addResourceSet(resourceModel.getResources());
    }

    private void initInputResourceSetsPresenter() {
        inputResourceSetsPresenter.init();

        resourceModel.getCombinedUserResourceSets().addEventHandler(
                new ResourceSetAddedEventHandler() {
                    @Override
                    public void onResourceSetAdded(ResourceSetAddedEvent e) {
                        ResourceSet resources = e.getResourceSet();
                        inputResourceSetsPresenter.addResourceSet(resources);
                    }
                });
        resourceModel.getCombinedUserResourceSets().addEventHandler(
                new ResourceSetRemovedEventHandler() {
                    @Override
                    public void onResourceSetRemoved(ResourceSetRemovedEvent e) {
                        ResourceSet resources = e.getResourceSet();
                        inputResourceSetsPresenter.removeResourceSet(resources);
                    }
                });
    }

}
