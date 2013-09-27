package org.thechiselgroup.biomixer.client.services.ontology_overview;

public class OntologyMappingCount {
    private String sourceOntologyAcronym;

    private String targetOntologyAcronym;

    private int sourceMappingCount;

    public OntologyMappingCount(String sourceOntologyAcronym,
            String targetOntologyAcronym, int sourceMappingCount) {
        this.sourceOntologyAcronym = sourceOntologyAcronym;
        this.targetOntologyAcronym = targetOntologyAcronym;
        this.sourceMappingCount = sourceMappingCount;
    }

    public int getSourceMappingCount() {
        return sourceMappingCount;
    }

    public String getSourceOntologyAcronym() {
        return sourceOntologyAcronym;
    }

    public String getTargetOntologyAcronym() {
        return targetOntologyAcronym;
    }

}
