package org.thechiselgroup.biomixer.client.services.ontology_version;

import org.thechiselgroup.biomixer.client.services.AbstractXMLResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;

import com.google.inject.Inject;

public class OntologyVersionParser extends AbstractXMLResultParser {

    @Inject
    public OntologyVersionParser(DocumentProcessor documentProcessor) {
        super(documentProcessor);
    }

    public String parse(String virtualOntologyId, String xmlText)
            throws Exception {
        return getText(parseDocument(xmlText),
                "//success/data/ontologyBean/id/text()");
    }

}
