package org.thechiselgroup.biomixer.client.services.ontology_overview;

public class OntologyMappingCount {
    private String id1;

    private String id2;

    private String count;

    public OntologyMappingCount(String id1, String id2, String count) {
        this.id1 = id1;
        this.id2 = id2;
        this.count = count;
    }

    public String getCount() {
        return count;
    }

    public String getId1() {
        return id1;
    }

    public String getId2() {
        return id2;
    }

}
