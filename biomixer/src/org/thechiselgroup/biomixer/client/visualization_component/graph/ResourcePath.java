package org.thechiselgroup.biomixer.client.visualization_component.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.Concept;
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
                Concept.PARENT_CONCEPTS);

        // Repeat until root is found (has no more parents)
        while (parents != null) {
            for (String parentUri : parents) {
                // Use URI to find Resource object
                for (Resource resource : resources) {
                    if (resource.getUri().equals(parentUri)) {
                        /* Found the next step in the path */
                        pathResources.add(resource);
                        currentResource = resource;
                        break;
                    }
                }

                parents = (UriList) currentResource.getProperties().get(
                        Concept.PARENT_CONCEPTS);
            }
        }

        cachedPathResources = pathResources;
        return pathResources;
    }

    /**
     * 
     * @return Returns all resources found when parsing NCBO rest service
     *         response, EXCEPT the target. This may include resources that are
     *         not directly on the path to the root.
     */
    public List<Resource> getSurroundingResources() {
        return resources;
    }

    public Resource getTarget() {
        return target;
    }

}
