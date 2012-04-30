package org.thechiselgroup.biomixer.client.services.ontology_overview;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface OntologyMappingCountServiceAsync {
    void getMappingCounts(List<String> virtualOntologyIds,
            AsyncCallback<TotalMappingCount> callback);

}
