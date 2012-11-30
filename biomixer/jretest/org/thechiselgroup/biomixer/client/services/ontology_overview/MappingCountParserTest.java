package org.thechiselgroup.biomixer.client.services.ontology_overview;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class MappingCountParserTest {

    private OntologyMappingCountXMLParser underTest;

    private TotalMappingCount getOntologyMappingCounts(String xmlFilename)
            throws Exception {
        String responseXml = IOUtils
                .readIntoString(MappingCountParserTest.class
                        .getResourceAsStream(xmlFilename));

        return underTest.parseForCount(responseXml);
    }

    @Test
    public void parseOntologyCounts() throws Exception {
        TotalMappingCount ontologyMappingCounts = getOntologyMappingCounts("ontology_mapping_count.response");

        assertThat(ontologyMappingCounts.size(), equalTo(6));

        List<Integer> counts = new ArrayList<Integer>();

        for (OntologyMappingCount mappingCount : ontologyMappingCounts) {
            counts.add(mappingCount.getCount());
        }
        assertThat(counts,
                containsExactly(Arrays.asList(11, 100, 11, 4828, 100, 10627)));
    }

    @Before
    public void setUp() throws Exception {
        underTest = new OntologyMappingCountXMLParser(
                new StandardJavaXMLDocumentProcessor());
    }

    @Test
    public void testTotalCount() throws Exception {
        TotalMappingCount ontologyMappingCounts = getOntologyMappingCounts("ontology_mapping_count.response");

        assertThat(ontologyMappingCounts.size(), equalTo(6));

        int mappingCount = ontologyMappingCounts
                .getMappingCount("1009", "1032");
        int mappingCount2 = ontologyMappingCounts.getMappingCount("1032",
                "1009");

        assertEquals(15455, mappingCount);
        assertEquals(15455, mappingCount2);
    }

}
