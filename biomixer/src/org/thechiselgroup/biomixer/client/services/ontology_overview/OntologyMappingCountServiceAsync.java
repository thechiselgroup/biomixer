package org.thechiselgroup.biomixer.client.services.ontology_overview;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface OntologyMappingCountServiceAsync {
    void getMappingCounts(Iterable<String> ontologyAcronyms,
            AsyncCallback<TotalMappingCount> callback);

    void getAllMappingCountsForCentralOntology(String ontologyAcronym,
            AsyncCallback<TotalMappingCount> callback);

}
