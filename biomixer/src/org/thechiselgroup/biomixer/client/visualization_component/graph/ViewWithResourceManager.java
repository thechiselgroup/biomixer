package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.RequiresAutomaticResourceSet;

/**
 * Stumbled upon this as a solution around multiple inheritance when refactoring
 * resource service stuff to be more general than Graph.
 * 
 * @author everbeek
 * 
 */
public interface ViewWithResourceManager {

    public SpecializedResourceManager getSpecificResourceManager();

    static abstract public class SpecializedResourceManager {

        final protected ResourceCategorizer resourceCategorizer;

        final protected ResourceManager resourceManager;

        private RequiresAutomaticResourceSet automaticResourceOwner;

        public SpecializedResourceManager(ResourceManager resourceManager,
                ResourceCategorizer resourceCategorizer,
                RequiresAutomaticResourceSet automaticResourceOwner) {
            this.resourceManager = resourceManager;
            this.resourceCategorizer = resourceCategorizer;
            this.automaticResourceOwner = automaticResourceOwner;
        }

        abstract public boolean containsResourceWithUri(String resourceUri);

        abstract public Resource getResourceByUri(String resourceUri);

        public void addAutomaticResource(Resource resource) {
            automaticResourceOwner.getAutomaticResources().add(resource);
        }

        public String getCategory(Resource resource) {
            return resourceCategorizer.getCategory(resource);
        }

        /**
         * @deprecated {@link ResourceManager} should be injected instead.
         */
        @Deprecated
        public ResourceManager getResourceManager() {
            return resourceManager;
        }
    }

}
