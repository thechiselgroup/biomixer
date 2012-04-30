package org.thechiselgroup.biomixer.client.services.ontology_overview;

import org.thechiselgroup.biomixer.client.services.AbstractXMLResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;

public class OntologyMappingCountParser extends AbstractXMLResultParser {

    public OntologyMappingCountParser(DocumentProcessor documentProcessor) {
        super(documentProcessor);

    }

    public TotalMappingCount parse(String xmlText) throws Exception {
        Object rootNode = parseDocument(xmlText);
        Object[] statistics = getNodes(rootNode,
                "//success/data/set/ontologyPairMappingStatistics");
        TotalMappingCount ontologyMappingCounts = new TotalMappingCount();
        for (Object statistic : statistics) {

            String sourceOntologyId = getText(statistic, "sourceOntologyId");
            String targetOntologyId = getText(statistic, "targetOntologyId");
            String mappingCount = getText(statistic, "mappingCount");

            ontologyMappingCounts.add(new OntologyMappingCount(
                    sourceOntologyId, targetOntologyId, mappingCount));
        }

        return ontologyMappingCounts;

    }

}
