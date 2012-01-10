package org.thechiselgroup.biomixer.client.services.ontology_version;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface OntologyVersionServiceAsync {

    /*
     * A virtual ontology id always gets the latest version of the ontology.
     * This is used for most of the services such as concept neighbourhoods,
     * etc. However, some of the NCBO REST services such as finding the path to
     * root don't have an option for using a virtual ontology id. They must use
     * a specific ontology version id. This is the interface for converting a
     * virtual ontology id to a specific ontology version id.
     */
    public void getOntologyVersionId(String virtualOntologyId,
            AsyncCallback<String> callback);

}
