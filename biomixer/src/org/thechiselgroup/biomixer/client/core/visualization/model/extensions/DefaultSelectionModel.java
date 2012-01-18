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
package org.thechiselgroup.biomixer.client.core.visualization.model.extensions;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.label.LabelProvider;
import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.Persistable;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.resources.NullResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ProxyResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetAddedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetAddedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetRemovedEvent;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetRemovedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetCollector;
import org.thechiselgroup.biomixer.client.core.util.Disposable;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class DefaultSelectionModel implements SelectionModel, Disposable,
        Persistable {

    static final String MEMENTO_SELECTION = "selection";

    static final String MEMENTO_SELECTION_SET_COUNT = "selectionSetCount";

    static final String MEMENTO_SELECTION_SET_PREFIX = "selectionSet-";

    private List<ResourceSet> selectionSets = new ArrayList<ResourceSet>();

    private ProxyResourceSet selection = new ProxyResourceSet();

    private LabelProvider selectionModelLabelFactory;

    private final ResourceSetFactory resourceSetFactory;

    protected transient HandlerManager eventBus;

    public DefaultSelectionModel(LabelProvider selectionModelLabelFactory,
            ResourceSetFactory resourceSetFactory) {

        assert selectionModelLabelFactory != null;
        assert resourceSetFactory != null;

        this.selectionModelLabelFactory = selectionModelLabelFactory;
        this.resourceSetFactory = resourceSetFactory;

        eventBus = new HandlerManager(this);
    }

    @Override
    public HandlerRegistration addEventHandler(
            ResourceSetActivatedEventHandler handler) {
        return eventBus.addHandler(ResourceSetActivatedEvent.TYPE, handler);
    }

    @Override
    public HandlerRegistration addEventHandler(
            ResourceSetAddedEventHandler handler) {
        return eventBus.addHandler(ResourceSetAddedEvent.TYPE, handler);
    }

    @Override
    public HandlerRegistration addEventHandler(
            ResourceSetChangedEventHandler handler) {
        return selection.addEventHandler(handler);
    }

    @Override
    public HandlerRegistration addEventHandler(
            ResourceSetRemovedEventHandler handler) {
        return eventBus.addHandler(ResourceSetRemovedEvent.TYPE, handler);
    }

    @Override
    public void addSelectionSet(ResourceSet selectionSet) {
        assert selectionSet != null;
        assert !selectionSets.contains(selectionSet);

        selectionSets.add(selectionSet);
        eventBus.fireEvent(new ResourceSetAddedEvent(selectionSet));
    }

    @Override
    public boolean containsSelectionSet(ResourceSet resourceSet) {
        return selectionSets.contains(resourceSet);
    }

    @Override
    public void dispose() {
        selection.dispose();
        selection = null;
    }

    @Override
    public ResourceSet getSelection() {
        return selection.getDelegate();
    }

    @Override
    public ResourceSet getSelectionProxy() {
        return selection;
    }

    public List<ResourceSet> getSelectionSets() {
        return selectionSets;
    }

    @Override
    public void removeSelectionSet(ResourceSet selectionSet) {
        assert selectionSet != null;
        assert selectionSets.contains(selectionSet);

        selectionSets.remove(selectionSet);
        eventBus.fireEvent(new ResourceSetRemovedEvent(selectionSet));
    }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {

        int selectionSetCount = (Integer) state
                .getValue(MEMENTO_SELECTION_SET_COUNT);
        for (int i = 0; i < selectionSetCount; i++) {
            addSelectionSet(restoreResourceSet(state, accessor,
                    MEMENTO_SELECTION_SET_PREFIX + i));
        }

        if (state.getValue(MEMENTO_SELECTION) != null) {
            setSelection(restoreResourceSet(state, accessor, MEMENTO_SELECTION));
        }
    }

    private ResourceSet restoreResourceSet(Memento state,
            ResourceSetAccessor accessor, String key) {
        int id = (Integer) state.getValue(key);
        ResourceSet resourceSet = accessor.getResourceSet(id);
        return resourceSet;
    }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        Memento memento = new Memento();

        memento.setValue(MEMENTO_SELECTION_SET_COUNT, selectionSets.size());

        for (int i = 0; i < selectionSets.size(); i++) {
            storeResourceSet(resourceSetCollector, memento,
                    MEMENTO_SELECTION_SET_PREFIX + i, selectionSets.get(i));
        }

        if (selection.hasDelegate()) {
            storeResourceSet(resourceSetCollector, memento, MEMENTO_SELECTION,
                    getSelection());
        }

        return memento;
    }

    @Override
    public void setSelection(ResourceSet newSelectionModel) {
        assert newSelectionModel == null
                || newSelectionModel instanceof NullResourceSet
                || selectionSets.contains(newSelectionModel) : "selection "
                + newSelectionModel + " not contained in this view";

        selection.setDelegate(newSelectionModel);
        eventBus.fireEvent(new ResourceSetActivatedEvent(newSelectionModel));
    }

    private void storeResourceSet(ResourceSetCollector persistanceManager,
            Memento memento, String key, ResourceSet resources) {

        memento.setValue(key, persistanceManager.storeResourceSet(resources));
    }

    // TODO this means that we need a wrapper around resource set
    // to make this happen
    @Override
    public void switchSelection(ResourceSet resources) {
        // XXX HACK TODO cleanup --> we create selections when stuff
        // gets selected...
        if (!selection.hasDelegate()) {
            ResourceSet set = resourceSetFactory.createResourceSet();
            set.setLabel(selectionModelLabelFactory.nextLabel());
            addSelectionSet(set);
            setSelection(set);
        }

        assert selection != null;

        // Partial selection: should be able to go to fully selected
        if (selection.containsNone(resources)
                || selection.containsAll(resources)) {
            getSelection().invertAll(resources);
        } else {
            getSelection().addAll(resources);
        }
    }
}
