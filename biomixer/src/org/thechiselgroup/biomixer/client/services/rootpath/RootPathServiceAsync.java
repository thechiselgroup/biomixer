package org.thechiselgroup.biomixer.client.services.rootpath;

import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourcePath;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RootPathServiceAsync {

    void findPathToRoot(String ontologyVersionId, String virtualOntologyId,
            String conceptId, AsyncCallback<ResourcePath> callback);

}
