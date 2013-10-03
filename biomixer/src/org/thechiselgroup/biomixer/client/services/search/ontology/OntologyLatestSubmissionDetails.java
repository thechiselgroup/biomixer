package org.thechiselgroup.biomixer.client.services.search.ontology;

/**
 * Contains statistics for an ontology. First made to support storage of results
 * from query to ontology statistics REST call.
 * 
 * @author everbeek
 * 
 */
public class OntologyLatestSubmissionDetails {

    public String description;

    final public String ontologyAcronym;

    public String version;

    public Integer submissionId;

    public boolean latest;

    public OntologyLatestSubmissionDetails(String ontologyAcronym) {
        this.ontologyAcronym = ontologyAcronym;
    }

}
