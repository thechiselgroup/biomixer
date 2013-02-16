package org.thechiselgroup.biomixer.client.services.ontology_overview;

public class OntologyMappingCount {
    private String sourceId;

    private String targetId;

    private int sourceMappingCount;

    private int targetMappingCount;

    public OntologyMappingCount(String sourceId, String targetId,
            int sourceMappingCount, int targetMappingCount) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.sourceMappingCount = sourceMappingCount;
        this.targetMappingCount = targetMappingCount;
    }

    public int getSourceMappingCount() {
        return sourceMappingCount;
    }

    public int getTargetMappingCount() {
        return targetMappingCount;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

}
