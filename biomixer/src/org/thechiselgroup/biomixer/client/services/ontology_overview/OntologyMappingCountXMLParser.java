package org.thechiselgroup.biomixer.client.services.ontology_overview;

import org.thechiselgroup.biomixer.client.services.AbstractXMLResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;

import com.google.inject.Inject;

public class OntologyMappingCountXMLParser extends AbstractXMLResultParser {

    @Inject
    public OntologyMappingCountXMLParser(DocumentProcessor documentProcessor) {
        super(documentProcessor);

    }

    public TotalMappingCount parseForCount(String xmlText) throws Exception {
        Object rootNode = parseDocument(xmlText);
        Object[] statistics = getNodes(rootNode,
                "//success/data/set/ontologyPairMappingStatistics");
        TotalMappingCount ontologyMappingCounts = new TotalMappingCount();
        for (Object statistic : statistics) {
            // XXX The JSON output appears to be changed relative to this
            // one...different server/URL used?
            String sourceOntologyId = getText(statistic, "sourceOntologyId");
            String targetOntologyId = getText(statistic, "targetOntologyId");
            int mappingCount = Integer.parseInt(getText(statistic,
                    "mappingCount"));

            // XXX Fix this, base it off of OntologyMappingCountJSONParser,
            // which should work already with the new API.
            // We enedn't fix this XML, but it is crufty and we can indeed
            // delete it. Making a ticket.

            ontologyMappingCounts.add(new OntologyMappingCount(
                    sourceOntologyId, targetOntologyId, mappingCount));
        }

        return ontologyMappingCounts;

    }

}
