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
package org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.parser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.svg.XmlTestUtils;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class SvgResultParserTest {

    private SvgResultParser underTest;

    // Element type "svg" must be followed by either attribute specifications,
    // ">" or "/>"
    @Ignore("TODO fix parsing error")
    @Test
    public void extractElementAsText() throws Exception {
        XmlTestUtils.assertXmlEquals(getXMLFromFile("expected_extraction.xml"),
                underTest.extractElementAsString(
                        getXMLFromFile("svg_parser_test.svg"), "//svg/svg"));
    }

    private String getXMLFromFile(String xmlFilename) throws IOException {
        return IOUtils.readIntoString(SvgResultParserTest.class
                .getResourceAsStream(xmlFilename));
    }

    @Before
    public void setUp() throws ParserConfigurationException {
        underTest = new SvgResultParser(new StandardJavaXMLDocumentProcessor());
    }
}
