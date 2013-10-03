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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.core.util.date.SimpleDateTimeFormatFactory;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class MappingResponseParserTest {

    @Test
    public void parseResponse() throws Exception {
        MappingResponseParser underTest = new MappingResponseParser(
                new StandardJavaXMLDocumentProcessor(),
                new SimpleDateTimeFormatFactory());
        String responseXML = IOUtils
                .readIntoString(MappingResponseParserTest.class
                        .getResourceAsStream("mapping-service.response"));

        List<Resource> parsedMappings = underTest.parseMapping(responseXML);

        assertThat(parsedMappings.size(), is(1));

        Resource mapping = parsedMappings.get(0);

        assertThat(
                mapping.getUri(),
                equalTo(Mapping
                        .toMappingURI("http://purl.bioontology.org/mapping/61d3af40-0060-012e-74a1-005056bd0010")));

        assertThat(
                (String) mapping.getValue(Mapping.ID),
                equalTo("http://purl.bioontology.org/mapping/61d3af40-0060-012e-74a1-005056bd0010"));

        assertThat((String) mapping.getValue(Mapping.SOURCE),
                equalTo(Concept.toConceptURI("1009",
                        "http://purl.org/obo/owl/DOID#DOID_0000000")));
        assertThat(
                (String) mapping.getValue(Mapping.TARGET),
                equalTo(Concept
                        .toConceptURI("1032",
                                "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Gallbladder_Disorder")));

        // assertThat((Date) mapping.getValue(Mapping.DATE),
        // equalTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z")
        // .parse("2010-05-17 16:24:34.0 PDT")));
        //
        // assertThat((String) mapping.getValue(Mapping.MAPPING_TYPE),
        // equalTo("Automatic"));
        // assertThat((String) mapping.getValue(Mapping.MAPPING_SOURCE),
        // equalTo("APPLICATION"));
        // assertThat((String) mapping.getValue(Mapping.MAPPING_SOURCE_NAME),
        // equalTo("LOOM"));
    }

}