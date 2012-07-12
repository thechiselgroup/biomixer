/*******************************************************************************
 * Copyright 2012 David Rusk 
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.AbstractJsonParserTest;
import org.thechiselgroup.biomixer.server.core.util.date.SimpleDateTimeFormatFactory;
import org.thechiselgroup.biomixer.server.workbench.util.json.JavaJsonParser;

public class MappingResponseJsonParserTest extends AbstractJsonParserTest {

    private MappingResponseJsonParser underTest;

    private Resource getMappingWithUri(String uri, List<Resource> mappings) {
        for (Resource mapping : mappings) {
            if (mapping.getUri().equals(uri)) {
                return mapping;
            }
        }
        Assert.fail();
        return null;
    }

    @Test
    public void parseAutomaticMapping() throws IOException, ParseException {
        List<Resource> parsedMappings = parseMappings("new-mapping-service.json");
        assertThat(parsedMappings.size(), is(2));

        String testMappingId = "http://purl.bioontology.org/mapping/fc69b2c0-f207-012d-745e-005056bd0010";
        Resource mapping = getMappingWithUri(
                Mapping.toMappingURI(testMappingId), parsedMappings);

        assertThat(mapping.getUri(),
                equalTo(Mapping.toMappingURI(testMappingId)));

        assertThat(
                (String) mapping.getValue(Mapping.ID),
                equalTo("http://purl.bioontology.org/mapping/fc69b2c0-f207-012d-745e-005056bd0010"));

        assertThat((String) mapping.getValue(Mapping.SOURCE),
                equalTo(Concept.toConceptURI("1009",
                        "http://purl.org/obo/owl/DOID#DOID_0000000")));
        assertThat(
                (String) mapping.getValue(Mapping.TARGET),
                equalTo(Concept
                        .toConceptURI("1245",
                                "http://purl.bioontology.org/ontology/MCCL/DOID_0000000")));

        assertThat((Date) mapping.getValue(Mapping.DATE),
                equalTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z")
                        .parse("2010-05-17 16:24:34.0 PDT")));

        assertThat((String) mapping.getValue(Mapping.MAPPING_TYPE),
                equalTo("Automatic"));
        assertThat((String) mapping.getValue(Mapping.MAPPING_SOURCE),
                equalTo("APPLICATION"));
        assertThat((String) mapping.getValue(Mapping.MAPPING_SOURCE_NAME),
                equalTo("LOOM"));
    }

    @Test
    public void parseManualMapping() throws IOException, ParseException {
        List<Resource> parsedMappings = parseMappings("new-mapping-service.json");
        assertThat(parsedMappings.size(), is(2));

        String testMappingId = "http://purl.bioontology.org/mapping/bbbdaca0-f1f4-012d-745c-005056bd0010";

        Resource mapping = getMappingWithUri(
                Mapping.toMappingURI(testMappingId), parsedMappings);

        assertThat((String) mapping.getValue(Mapping.ID),
                equalTo(testMappingId));

        assertThat((String) mapping.getValue(Mapping.SOURCE),
                equalTo(Concept.toConceptURI("1009",
                        "http://purl.org/obo/owl/DOID#DOID_0000000")));
        assertThat((String) mapping.getValue(Mapping.TARGET),
                equalTo(Concept.toConceptURI("1101",
                        "http://purl.bioontology.org/ontology/ICD-9/575.9")));

        assertThat((Date) mapping.getValue(Mapping.DATE),
                equalTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z")
                        .parse("2008-05-04 16:58:25.0 PDT")));

        assertThat((String) mapping.getValue(Mapping.MAPPING_TYPE),
                equalTo("Manual"));
        assertNull(mapping.getValue(Mapping.MAPPING_SOURCE));
        assertNull(mapping.getValue(Mapping.MAPPING_SOURCE_NAME));
    }

    private List<Resource> parseMappings(String jsonFilename)
            throws IOException {
        return underTest.parseMapping(getFileContentsAsString(jsonFilename));
    }

    @Test
    public void parseResponseWithNoMappings() throws IOException {
        List<Resource> parsedMappings = parseMappings("no_mappings_response.json");
        assertThat(parsedMappings.size(), is(0));
    }

    @Before
    public void setUp() {
        underTest = new MappingResponseJsonParser(new JavaJsonParser(),
                new SimpleDateTimeFormatFactory());
    }

}
