package org.thechiselgroup.biomixer.client.visualization_component.graph;

import java.io.Serializable;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.resources.Resource;

public class ResourcePath implements Serializable {

    /**
     * Generated UID
     */
    private static final long serialVersionUID = -4844542971601943248L;

    private final Resource target;

    private final List<Resource> resources;

    public ResourcePath(Resource target, List<Resource> resources) {
        this.target = target;
        this.resources = resources;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public Resource getTarget() {
        return target;
    }

}
