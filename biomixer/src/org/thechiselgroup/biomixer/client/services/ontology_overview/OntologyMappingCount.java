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

    /**
     * Sometimes only the total mapping count is available fromt he REST
     * service.
     * 
     * @param sourceId
     * @param targetId
     * @param totalMappingCount
     */
    public OntologyMappingCount(String sourceId, String targetId,
            int totalMappingCount) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        // Ensure that they sum (rather than dividing by 2 for both)
        int sourceMapCount = totalMappingCount - totalMappingCount / 2;
        this.sourceMappingCount = sourceMapCount;
        this.targetMappingCount = totalMappingCount - sourceMapCount;
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
