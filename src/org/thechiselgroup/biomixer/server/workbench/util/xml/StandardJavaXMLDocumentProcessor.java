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
package org.thechiselgroup.biomixer.server.workbench.util.xml;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.XPathEvaluationException;
import org.w3c.dom.NodeList;

/**
 * {@link DocumentProcessor} implementation that uses the standard java xpath
 * facilities.
 * 
 * @author Lars Grammel
 */
public class StandardJavaXMLDocumentProcessor implements DocumentProcessor {

    private final DocumentBuilder domBuilder;

    private final XPathFactory xPathFactory;

    public StandardJavaXMLDocumentProcessor()
            throws ParserConfigurationException {

        domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        xPathFactory = XPathFactory.newInstance();
    }

    @Override
    public Object[] getNodes(Object node, String xpath)
            throws XPathEvaluationException {

        try {
            NodeList nodes = (NodeList) xPathFactory.newXPath().evaluate(xpath,
                    node, XPathConstants.NODESET);
            Object[] result = new Object[nodes.getLength()];
            for (int i = 0; i < result.length; i++) {
                result[i] = nodes.item(i);
            }
            return result;
        } catch (XPathExpressionException e) {
            throw new XPathEvaluationException(node, xpath, e);
        }
    }

    @Override
    public String getText(Object node, String xpath)
            throws XPathEvaluationException {
        try {
            return (String) xPathFactory.newXPath().evaluate(xpath, node,
                    XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new XPathEvaluationException(node, xpath, e);
        }
    }

    @Override
    public Object parseDocument(String xmlText) throws Exception {
        return domBuilder.parse(new ByteArrayInputStream(xmlText.getBytes()));
    }

}