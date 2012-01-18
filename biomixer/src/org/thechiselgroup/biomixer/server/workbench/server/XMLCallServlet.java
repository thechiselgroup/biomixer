/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.server.workbench.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.ServiceException;
import org.thechiselgroup.biomixer.server.workbench.urlfetch.DefaultDocumentFetchService;
import org.thechiselgroup.biomixer.server.workbench.urlfetch.DocumentFetchService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public abstract class XMLCallServlet extends RemoteServiceServlet {

    protected DocumentFetchService documentFetchService;

    private HashMap<String, XPathExpression> expressions = new HashMap<String, XPathExpression>();

    private XPathExpression setExpression;

    protected XPath xpath;

    protected abstract Resource analyzeNode(Node node, String label)
            throws Exception;

    public Set<Resource> analyzeXML(String url, String label)
            throws ServiceException {

        try {
            Set<Resource> resources = new HashSet<Resource>();

            NodeList nodes = getSetExpressionNodes(url);
            for (int i = 0; i < nodes.getLength(); i++) {
                resources.add(analyzeNode(nodes.item(i), label));
            }

            return resources;

        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }

    }

    public Number evaluateNumber(String expressionKey, Node node)
            throws XPathExpressionException {

        return (Number) expressions.get(expressionKey).evaluate(node,
                XPathConstants.NUMBER);
    }

    public String evaluateString(String expressionKey, Node node)
            throws XPathExpressionException {

        return (String) expressions.get(expressionKey).evaluate(node,
                XPathConstants.STRING);
    }

    protected NodeList getSetExpressionNodes(String url) throws SAXException,
            IOException, XPathExpressionException, ParserConfigurationException {

        Document document = documentFetchService.fetchXML(url);
        return (NodeList) setExpression.evaluate(document,
                XPathConstants.NODESET);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // FIXME: workaround for app engine issue 1255
        // http://code.google.com/p/googleappengine/issues/detail?id=1255
        // XPathFactory factory = XPathFactory.newInstance();
        XPathFactory factory = new org.apache.xpath.jaxp.XPathFactoryImpl();

        xpath = factory.newXPath();

        DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory
                .newInstance();
        domBuilderFactory.setNamespaceAware(true);

        // TODO use regular fetch service for production
        documentFetchService = new DefaultDocumentFetchService(
                URLFetchServiceFactory.getURLFetchService(), domBuilderFactory);
        // documentFetchService = new CachedDocumentFetchService(
        // URLFetchServiceFactory.getURLFetchService(), PMF.get(),
        // domBuilderFactory);
    }

    public void registerExpression(String key, String expression)
            throws ServletException {

        try {
            expressions.put(key, xpath.compile(expression));
        } catch (XPathExpressionException e) {
            throw new ServletException(e);
        }
    }

    protected void setupSetExpression(String setExpression)
            throws ServletException {
        try {
            this.setExpression = xpath.compile(setExpression);
        } catch (XPathExpressionException e) {
            throw new ServletException(e);
        }
    }

}
