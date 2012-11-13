package org.thechiselgroup.biomixer.client.services.ontology_overview;

public class OntologyMappingCount {
    private String sourceId;

    private String targetId;

    private String count;

    public OntologyMappingCount(String sourceId, String targetId, String count) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.count = count;
    }

    public String getCount() {
        return count;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

}
