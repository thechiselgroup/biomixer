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

import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetAddedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetAddedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetRemovedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetRemovedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetsPresenter;
import org.thechiselgroup.biomixer.client.core.ui.Presenter;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceSetActivatedEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceSetActivatedEventHandler;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.SelectionModel;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DefaultSelectionModelPresenter implements Presenter {

    private ResourceSetsPresenter selectionDropPresenter;

    private ResourceSetsPresenter selectionPresenter;

    private SelectionModel selectionModel;

    public DefaultSelectionModelPresenter(
            ResourceSetsPresenter selectionDropPresenter,
            ResourceSetsPresenter selectionPresenter,
            SelectionModel selectionModel) {

        assert selectionPresenter != null;
        assert selectionDropPresenter != null;
        assert selectionModel != null;

        this.selectionDropPresenter = selectionDropPresenter;
        this.selectionPresenter = selectionPresenter;
        this.selectionModel = selectionModel;
    }

    @Override
    public Widget asWidget() {
        HorizontalPanel panel = new HorizontalPanel();

        panel.add(selectionPresenter.asWidget());
        panel.add(selectionDropPresenter.asWidget());

        return panel;
    }

    @Override
    public void dispose() {
        selectionPresenter.dispose();
        selectionPresenter = null;
        selectionDropPresenter.dispose();
        selectionDropPresenter = null;
    }

    @Override
    public void init() {
        selectionPresenter.init();
        selectionDropPresenter.init();

        // TODO refactor: move to somewhere else (e.g. decorator)
        DefaultResourceSet resources = new DefaultResourceSet();
        resources.setLabel("add selection");
        selectionDropPresenter.addResourceSet(resources);

        initSelectionModelLink();
    }

    private void initSelectionModelLink() {
        selectionModel.addEventHandler(new ResourceSetAddedEventHandler() {
            @Override
            public void onResourceSetAdded(ResourceSetAddedEvent e) {
                ResourceSet resources = e.getResourceSet();
                selectionPresenter.addResourceSet(resources);
            }
        });
        selectionModel.addEventHandler(new ResourceSetRemovedEventHandler() {
            @Override
            public void onResourceSetRemoved(ResourceSetRemovedEvent e) {
                ResourceSet resources = e.getResourceSet();
                selectionPresenter.removeResourceSet(resources);
            }
        });
        selectionModel.addEventHandler(new ResourceSetActivatedEventHandler() {
            @Override
            public void onResourceSetActivated(ResourceSetActivatedEvent e) {
                ResourceSet resources = e.getResourceSet();
                selectionPresenter.setSelectedResourceSet(resources);
            }
        });
    }
}
