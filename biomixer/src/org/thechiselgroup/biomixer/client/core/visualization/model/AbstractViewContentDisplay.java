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
package org.thechiselgroup.biomixer.client.core.visualization.model;

import static org.thechiselgroup.biomixer.client.core.util.collections.Delta.createAddedDelta;
import static org.thechiselgroup.biomixer.client.core.util.collections.Delta.createRemovedDelta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetCollector;
import org.thechiselgroup.biomixer.client.core.ui.CSS;
import org.thechiselgroup.biomixer.client.core.ui.SidePanelSection;
import org.thechiselgroup.biomixer.client.core.util.DisposeUtil;
import org.thechiselgroup.biomixer.client.core.util.NoSuchAdapterException;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractViewContentDisplay implements ViewContentDisplay,
        VisualItemContainer {

    protected ViewContentDisplayCallback callback;

    private boolean restoring = false;

    private Widget widget;

    private final Map<String, ViewContentDisplayProperty<?>> properties = CollectionFactory
            .createStringMap();

    private VisualItemContainer container;

    private State state = State.CREATED;

    private List<ViewResizeEventListener> resizeListeners = new ArrayList<ViewResizeEventListener>();

    @Override
    public <T> T adaptTo(Class<T> clazz) throws NoSuchAdapterException {
        throw new NoSuchAdapterException(this, clazz);
    }

    @Override
    public HandlerRegistration addHandler(
            VisualItemContainerChangeEventHandler handler) {
        return container.addHandler(handler);
    }

    public void addResizeListener(ViewResizeEventListener resizeListener) {
        resizeListeners.add(resizeListener);
    }

    private void assertInState(State expectedState) {
        assert isInState(expectedState) : "invalid state: " + state
                + " (should be " + expectedState + ")";
    }

    @Override
    public Widget asWidget() {
        if (widget == null) {
            widget = createWidget();

            widget.addAttachHandler(new Handler() {
                @Override
                public void onAttachOrDetach(AttachEvent event) {
                    if (event.isAttached()) {
                        onAttach();
                    } else {
                        onDetach();
                    }
                }
            });
        }

        return widget;
    }

    @Override
    public boolean containsVisualItem(String visualItemId) {
        assertInState(State.INITIALIZED);
        return container.containsVisualItem(visualItemId);
    }

    protected abstract Widget createWidget();

    @Override
    public void dispose() {
        setState(State.DISPOSING);

        callback = null;
        container = null;
        widget = DisposeUtil.dispose(widget);

        setState(State.DISPOSED);
        assertInState(State.DISPOSED);
    }

    @Override
    public void endRestore() {
        restoring = false;
    }

    private void fireResizeEvent(ViewResizeEvent resizeEvent) {
        for (ViewResizeEventListener resizeListener : resizeListeners) {
            resizeListener.onResize(resizeEvent);
        }
    }

    public ViewContentDisplayCallback getCallback() {
        return callback;
    }

    public VisualItemContainer getContainer() {
        return container;
    }

    @SuppressWarnings("unchecked")
    private <T> ViewContentDisplayProperty<T> getProperty(String property) {
        assertInState(State.INITIALIZED);
        return (ViewContentDisplayProperty<T>) properties.get(property);
    }

    @Override
    public <T> T getPropertyValue(String property) {
        assertInState(State.INITIALIZED);
        // TODO NoSuchPropertyException extends RuntimeException
        if (!properties.containsKey(property)) {
            throw new IllegalArgumentException("Property '" + property
                    + "' does not exist.");
        }

        return this.<T> getProperty(property).getValue();
    }

    @Override
    public SidePanelSection[] getSidePanelSections() {
        return new SidePanelSection[0];
    }

    public State getState() {
        return state;
    };

    @Override
    public VisualItem getVisualItem(String visualItemId)
            throws NoSuchElementException {

        assertInState(State.INITIALIZED);
        return container.getVisualItem(visualItemId);
    }

    @Override
    public LightweightCollection<VisualItem> getVisualItems() {
        assertInState(State.INITIALIZED);
        return container.getVisualItems();
    }

    @Override
    public LightweightCollection<VisualItem> getVisualItems(
            Iterable<Resource> resources) {
        assert isInitialized();
        return container.getVisualItems(resources);
    }

    @Override
    public void init(VisualItemContainer container,
            ViewContentDisplayCallback callback) {

        assert container != null;
        assert callback != null;
        assertInState(State.CREATED);

        setState(State.INITIALIZING);

        this.callback = callback;
        this.container = container;

        setState(State.INITIALIZED);

        assertInState(State.INITIALIZED);
    }

    @Override
    public boolean isAdaptableTo(Class<?> clazz) {
        return false;
    }

    // TODO introduce state...
    protected boolean isAttached() {
        return widget != null && widget.isAttached();
    }

    public boolean isInitialized() {
        return isInState(State.INITIALIZED);
    }

    public boolean isInState(State expectedState) {
        return state.equals(expectedState);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    public boolean isRestoring() {
        return restoring;
    }

    protected void onAttach() {
        assert container != null;
        assert callback != null;

        if (!getVisualItems().isEmpty()) {
            /*
             * XXX the lifecycle should be exposed and the visualization model
             * should respect the lifecycle (this should be removed)
             */
            update(createAddedDelta(getVisualItems()),
                    LightweightCollections.<Slot> emptyCollection());
        }
    }

    protected void onDetach() {
        // might have been disposed (then callback would be null)
        if (container != null && !getVisualItems().isEmpty()) {
            /*
             * XXX this might be problematic, because view items are removed
             * when the visualization model is disposed.
             */
            update(createRemovedDelta(getVisualItems()),
                    LightweightCollections.<Slot> emptyCollection());
        }
    }

    /**
     * Adds a {@link ViewContentDisplayProperty} to the properties of this
     * {@link ViewContentDisplay}.
     */
    protected void registerProperty(ViewContentDisplayProperty<?> property) {
        assert property != null;
        this.properties.put(property.getPropertyName(), property);
    }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {
    }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        return new Memento();
    }

    @Override
    public <T> void setPropertyValue(String property, T value) {
        // TODO NoSuchPropertyException extends RuntimeException
        if (!properties.containsKey(property)) {
            throw new IllegalArgumentException("Property '" + property
                    + "' does not exist.");
        }

        getProperty(property).setValue(value);
    }

    @Override
    public void setSize(int width, int height) {
        widget.setSize(width + CSS.PX, height + CSS.PX);
        fireResizeEvent(new ViewResizeEvent(width, height, this));
    }

    private void setState(State state) {
        this.state = state;
    }

    @Override
    public void startRestore() {
        restoring = true;
    }

}
