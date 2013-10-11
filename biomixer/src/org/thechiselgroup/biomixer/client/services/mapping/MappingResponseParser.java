/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.services.mapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormat;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormatFactory;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.XPathEvaluationException;

import com.google.inject.Inject;

/**
 * Parses the response XML that is returned by the concept mapping service into
 * a set of mapping resources.
 * 
 * @author Lars Grammel
 */
public class MappingResponseParser {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.S z";

    private final DocumentProcessor documentProcessor;

    private final DateTimeFormat dateFormat;

    @Inject
    public MappingResponseParser(DocumentProcessor documentProcessor,
            DateTimeFormatFactory dateTimeFormatFactory) {

        this.documentProcessor = documentProcessor;
        this.dateFormat = dateTimeFormatFactory
                .createDateTimeFormat(DATE_PATTERN);
    }

    private String getText(Object node, String xpath)
            throws XPathEvaluationException {
        return documentProcessor.getText(node, xpath);
    }

    private Resource parseMapping(Object mappingNode)
            throws XPathEvaluationException {

        String id = getText(mappingNode, "id/text()");

        String sourceOntologyId = getText(mappingNode,
                "sourceOntologyId/text()");
        String sourceConceptId = getText(mappingNode, "source/fullId/text()");
        String sourceUri = Concept.toConceptURI(sourceOntologyId,
                sourceConceptId);

        String targetOntologyId = getText(mappingNode,
                "targetOntologyId/text()");
        String targetConceptId = getText(mappingNode, "target/fullId/text()");
        String targetUri = Concept.toConceptURI(targetOntologyId,
                targetConceptId);

        String mappingType = getText(mappingNode, "mappingType/text()");
        String mappingSource = getText(mappingNode, "mappingSource/text()");
        String mappingSourceName = getText(mappingNode,
                "mappingSourceName/text()");

        Date date = dateFormat.parse(getText(mappingNode, "date/text()"));

        Resource resource = Resource.createIndexedResource(Mapping
                .toMappingURI(id));

        resource.putValue(Mapping.ID, id);
        resource.putValue(Mapping.SOURCE_CONCEPT_URI, sourceUri);
        resource.putValue(Mapping.TARGET_CONCEPT_URI, targetUri);
        // resource.putValue(Mapping.DATE, date);
        // resource.putValue(Mapping.MAPPING_TYPE, mappingType);
        // resource.putValue(Mapping.MAPPING_SOURCE, mappingSource);
        // resource.putValue(Mapping.MAPPING_SOURCE_NAME, mappingSourceName);

        return resource;
    }

    public List<Resource> parseMapping(String mappingServiceResponse)
            throws Exception {

        Object responseDocument = documentProcessor
                .parseDocument(mappingServiceResponse);

        List<Resource> result = new ArrayList<Resource>();

        Object[] mappingNodes = documentProcessor.getNodes(responseDocument,
                "/success/data/page/contents/mappings/mapping");
        for (Object mappingNode : mappingNodes) {
            result.add(parseMapping(mappingNode));
        }

        return result;
    }
}