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
package org.thechiselgroup.biomixer.client.workbench.util.xml;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.shared.workbench.util.xml.DocumentProcessor;
import org.thechiselgroup.biomixer.shared.workbench.util.xml.XPathEvaluationException;

import com.google.gwt.core.client.JavaScriptObject;

public class SarissaDocumentProcessor implements DocumentProcessor {

    // @formatter:off
    private static native JavaScriptObject createDOMParser() /*-{
		return new $wnd.DOMParser();
    }-*/;
    // @formatter:on

    private JavaScriptObject domParser;

    private List<Node> doGetNodes(Object node, String xpath) {
        return ((Node) node).getNodes(xpath, new ArrayList<Node>());
    }

    // @formatter:off
    private native Node doParseDocument(String xmlText) /*-{
		var doc = this.@org.thechiselgroup.biomixer.client.workbench.util.xml.SarissaDocumentProcessor::domParser
				.parseFromString(xmlText, "text/xml");

		var parseErrorText = $wnd.Sarissa.getParseErrorText(doc);
		if (parseErrorText != $wnd.Sarissa.PARSED_OK) {
			throw (new Error(parseErrorText));
		}

		if (doc.setProperty) {
			doc.setProperty("SelectionNamespaces",
					"xmlns:xsl='http://www.w3.org/1999/XSL/Transform'");
			doc.setProperty("SelectionLanguage", "XPath");
		}

		return @org.thechiselgroup.biomixer.client.workbench.util.xml.Node::create(Lcom/google/gwt/core/client/JavaScriptObject;)(doc);
    }-*/;
    // @formatter:on

    @Override
    public Object[] getNodes(Object node, String xpath) {
        return doGetNodes(node, xpath).toArray();
    }

    @Override
    public String getText(Object node, String xpath)
            throws XPathEvaluationException {
        List<Node> nodes = doGetNodes(node, xpath);

        if (nodes.size() == 0) {
            throw new XPathEvaluationException(node, xpath);
        }

        return nodes.get(0).getValue().trim();
    }

    @Override
    public Object parseDocument(String xmlText) throws Exception {
        if (domParser == null) {
            domParser = createDOMParser();
        }

        return doParseDocument(xmlText);
    }
}