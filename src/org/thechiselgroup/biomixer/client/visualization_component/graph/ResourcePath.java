package org.thechiselgroup.biomixer.client.visualization_component.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;

public class ResourcePath implements Serializable {

    /**
     * Generated UID
     */
    private static final long serialVersionUID = -4844542971601943248L;

    private final Resource target;

    private final List<Resource> resources;

    private List<Resource> cachedPathResources;

    public ResourcePath(Resource target, List<Resource> resources) {
        this.target = target;
        this.resources = resources;
        this.cachedPathResources = null;
    }

    /**
     * Retrieves only those resources which are encountered on the path from the
     * target to the root, including the target and root.
     * 
     * @return List of resources on path to root
     */
    public List<Resource> getPathToRootResources() {
        if (cachedPathResources != null) {
            return cachedPathResources;
        }

        List<Resource> pathResources = new ArrayList<Resource>();

        /* Target should be the first thing on the path */
        Resource currentResource = target;
        pathResources.add(currentResource);

        UriList parents = (UriList) currentResource.getProperties().get(
                "parentConcepts");

        // Repeat until root is found (has no more parents)
        while (parents != null) {
            /* XXX just handle case of one parent for now */
            String parentUri = parents.getUri(0);

            // Use URI to find Resource object
            // XXX inefficient
            for (Resource resource : resources) {
                if (resource.getUri().equals(parentUri)) {
                    /* Found the next step in the path */
                    pathResources.add(resource);
                    currentResource = resource;
                    break;
                }
            }

            parents = (UriList) currentResource.getProperties().get(
                    "parentConcepts");
        }

        cachedPathResources = pathResources;
        return pathResources;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public Resource getTarget() {
        return target;
    }

}
