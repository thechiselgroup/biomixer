package org.thechiselgroup.biomixer.client.core.resources;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.util.Disposable;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

// TODO violates LSP --> need for ReadableResourceSet, WriteableResourceSet
public abstract class AbstractCombinedResourceSet extends DelegatingResourceSet
        implements Disposable {

    protected static class ResourceSetElement implements Disposable {

        private HandlerRegistration handlerRegistration;

        protected ResourceSet resourceSet;

        public ResourceSetElement(ResourceSet resourceSet) {
            this.resourceSet = resourceSet;
        }

        @Override
        public void dispose() {
            handlerRegistration.removeHandler();
        }

        private void registerHandler(ResourceSet resourceSet,
                ResourceSetChangedEventHandler handler) {

            handlerRegistration = resourceSet.addEventHandler(handler);
        }

    }

    protected ResourceSetChangedEventHandler resourceSetChangedHandler = new ResourceSetChangedEventHandler() {

        @Override
        public void onResourceSetChanged(ResourceSetChangedEvent event) {
            change(calculateResourcesToAdd(event.getAddedResources()),
                    calculateResourcesToRemove(event.getRemovedResources()));
        }

    };

    protected List<ResourceSetElement> containedResourceSets = new ArrayList<ResourceSetElement>();

    protected final HandlerManager eventBus;

    public AbstractCombinedResourceSet(ResourceSet delegate) {
        super(delegate);

        eventBus = new HandlerManager(this);
    }

    public HandlerRegistration addEventHandler(
            ResourceSetAddedEventHandler handler) {
        return eventBus.addHandler(ResourceSetAddedEvent.TYPE, handler);
    }

    public HandlerRegistration addEventHandler(
            ResourceSetRemovedEventHandler handler) {
        return eventBus.addHandler(ResourceSetRemovedEvent.TYPE, handler);
    }

    public void addResourceSet(ResourceSet resourceSet) {
        if (containsResourceSet(resourceSet)) {
            return;
        }

        ResourceSetElement resourceSetElement = new ResourceSetElement(
                resourceSet);
        containedResourceSets.add(resourceSetElement);

        doAdd(resourceSet);

        resourceSetElement.registerHandler(resourceSet,
                resourceSetChangedHandler);

        eventBus.fireEvent(new ResourceSetAddedEvent(resourceSet));
    }

    /**
     * Calculates which resources should be contained in this resource set. If
     * they are already contained, they might or might not be included in the
     * result.
     * 
     * @param resources
     *            resources to test if they should be contained
     * 
     * @return subset of the resources that should be contained in this resource
     *         set
     */
    protected abstract LightweightCollection<Resource> calculateResourcesToAdd(
            LightweightCollection<Resource> resources);

    /**
     * Calculates which resources should NOT be contained in this resource set.
     * If they are currently not contained, they might or might not be included
     * in the result.
     * 
     * @param addedResources
     *            resources to test if they should NOT be contained
     * 
     * @return subset of the resources that should NOT be contained in this
     *         resource set
     */
    protected abstract LightweightCollection<Resource> calculateResourcesToRemove(
            LightweightCollection<Resource> resources);

    @Override
    public void clear() {
        List<ResourceSetElement> toRemove = new ArrayList<ResourceSetElement>(
                containedResourceSets);

        for (ResourceSetElement resourceSetElement : toRemove) {
            removeResourceSet(resourceSetElement.resourceSet);
        }

        assert isEmpty();
    }

    public boolean containsResourceSet(ResourceSet resourceSet) {
        assert resourceSet != null;
        return getResourceSetElement(resourceSet) != null;
    }

    @Override
    public void dispose() {
        for (ResourceSetElement resourceSetElement : containedResourceSets) {
            resourceSetElement.dispose();
        }
    }

    protected abstract void doAdd(ResourceSet resourceSet);

    protected abstract void doRemove(ResourceSet resourceSet);

    protected ResourceSetElement getResourceSetElement(ResourceSet resources) {
        for (ResourceSetElement resourceSetElement : containedResourceSets) {
            if (resourceSetElement.resourceSet.equals(resources)) {
                return resourceSetElement;
            }
        }
        return null;
    }

    public List<ResourceSet> getResourceSets() {
        List<ResourceSet> resourceSets = new ArrayList<ResourceSet>();

        for (ResourceSetElement element : containedResourceSets) {
            resourceSets.add(element.resourceSet);
        }

        return resourceSets;
    }

    /**
     * @return false: should only be changed by adding / removing resource sets
     *         via {@link #addResourceSet(ResourceSet)} and
     *         {@link #removeResourceSet(ResourceSet)}
     */
    @Override
    public boolean isModifiable() {
        return false;
    }

    public void removeResourceSet(ResourceSet resourceSet) {
        ResourceSetElement resourceSetElement = getResourceSetElement(resourceSet);

        if (resourceSetElement == null) {
            return;
        }

        containedResourceSets.remove(resourceSetElement);
        resourceSetElement.dispose();

        doRemove(resourceSet);

        eventBus.fireEvent(new ResourceSetRemovedEvent(resourceSet));
    }

}