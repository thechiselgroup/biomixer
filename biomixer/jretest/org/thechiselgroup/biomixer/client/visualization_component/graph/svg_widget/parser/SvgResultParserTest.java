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
import org.junit.Test;
import org.thechiselgroup.biomixer.client.svg.XmlTestUtils;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.server.workbench.util.xml.StandardJavaXMLDocumentProcessor;

public class SvgResultParserTest {

    private SvgResultParser underTest;

    /**
     * 
     * @param expectedFileName
     *            name of the SVG file containing the expected output
     * @param inputFileName
     *            name of the SVG file containing the input for the parser
     * @param xpath
     *            the XPath expression specifying which elements should be
     *            selected from the input SVG
     * @throws IOException
     * @throws Exception
     */
    private void assertSvgEqualsSelection(String expectedFileName,
            String inputFileName, String xpath) throws IOException, Exception {
        XmlTestUtils.assertXmlEquals(getXMLFromFile(expectedFileName),
                underTest.extractElementAsString(getXMLFromFile(inputFileName),
                        xpath));
    }

    @Test
    public void basicNoChildrenRootElementHasNamespace() throws Exception {
        assertSvgEqualsSelection("expected_basic.svg", "basic_namespace.svg",
                "/svg/text");
    }

    @Test
    public void elementHasChildren() throws Exception {
        assertSvgEqualsSelection("expected_childNodes.svg", "childNodes.svg",
                "/svg/svg/*");
    }

    @Test
    public void elementHasNoChildren() throws Exception {
        assertSvgEqualsSelection("expected_basic.svg", "basic.svg", "/svg/text");
    }

    @Test
    public void extractElementWithSpecifiedAttribute() throws Exception {
        assertSvgEqualsSelection("expected_namedNode.svg", "namedNode.svg",
                "//svg[@id='n2']");
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
