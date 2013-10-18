/*******************************************************************************
 * Copyright 2012 Eric Verbeek
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
package org.thechiselgroup.biomixer.client.services.search.ontology;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.AbstractJsonParserTest;
import org.thechiselgroup.biomixer.server.workbench.util.json.JacksonJsonParser;

public class OntologyMetricsJsonParserTest extends AbstractJsonParserTest {

    private OntologyMetricJsonParser underTest;

    public List<String> getValues(Set<Resource> resources, String key) {
        List<String> values = new ArrayList<String>();
        for (Resource resource : resources) {
            values.add((String) resource.getValue(key));
        }
        return values;
    }

    @Test
    public void parseMetricResults() throws IOException {
        OntologyMetrics metricResults = parseSearchResults("ontologyMetricResults.json");

        assertThat(metricResults.maximumDepth, equalTo(11));
        assertThat(metricResults.numberOfClasses, equalTo(913));
        assertThat(metricResults.ontologyAcronym, equalTo("40133"));
    }

    public OntologyMetrics parseSearchResults(String jsonFilename)
            throws IOException {
        return underTest.parse(getFileContentsAsString(jsonFilename));
    }

    @Before
    public void setUp() {
        this.underTest = new OntologyMetricJsonParser(new JacksonJsonParser());
    }

}
