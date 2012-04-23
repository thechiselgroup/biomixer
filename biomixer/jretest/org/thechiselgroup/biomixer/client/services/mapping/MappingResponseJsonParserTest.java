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
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

    public MappingResponseJsonParserTest() {
        super(MappingResponseJsonParserTest.class);
    }

    private List<Resource> parseMappings(String jsonFilename)
            throws IOException {
        return underTest.parseMapping(getFileContentsAsString(jsonFilename));
    }

    @Test
    public void parseResponse() throws IOException, ParseException {
        List<Resource> parsedMappings = parseMappings("mapping-service.json");
        assertThat(parsedMappings.size(), is(3));

        Resource mapping1 = parsedMappings.get(0);

        assertThat(
                mapping1.getUri(),
                equalTo(Mapping
                        .toMappingURI("http://purl.bioontology.org/mapping/fc69b2c0-f207-012d-745e-005056bd0010")));

        assertThat(
                (String) mapping1.getValue(Mapping.ID),
                equalTo("http://purl.bioontology.org/mapping/fc69b2c0-f207-012d-745e-005056bd0010"));

        assertThat((String) mapping1.getValue(Mapping.SOURCE),
                equalTo(Concept.toConceptURI("1009",
                        "http://purl.org/obo/owl/DOID#DOID_0000000")));
        assertThat(
                (String) mapping1.getValue(Mapping.TARGET),
                equalTo(Concept
                        .toConceptURI("1245",
                                "http://purl.bioontology.org/ontology/MCCL/DOID_0000000")));

        assertThat((Date) mapping1.getValue(Mapping.DATE),
                equalTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z")
                        .parse("2010-05-17 16:24:34.0 PDT")));

        assertThat((String) mapping1.getValue(Mapping.MAPPING_TYPE),
                equalTo("Automatic"));
        assertThat((String) mapping1.getValue(Mapping.MAPPING_SOURCE),
                equalTo("APPLICATION"));
        assertThat((String) mapping1.getValue(Mapping.MAPPING_SOURCE_NAME),
                equalTo("LOOM"));
    }

    @Before
    public void setUp() {
        underTest = new MappingResponseJsonParser(new JavaJsonParser(),
                new SimpleDateTimeFormatFactory());
    }

}
