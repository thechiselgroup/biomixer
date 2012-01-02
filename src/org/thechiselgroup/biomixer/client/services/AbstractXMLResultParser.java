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
package org.thechiselgroup.biomixer.client.services;

import org.thechiselgroup.choosel.workbench.shared.util.xml.DocumentProcessor;
import org.thechiselgroup.choosel.workbench.shared.util.xml.XPathEvaluationException;

import com.google.inject.Inject;

public class AbstractXMLResultParser implements DocumentProcessor {

    private final DocumentProcessor documentProcessor;

    @Inject
    public AbstractXMLResultParser(DocumentProcessor documentProcessor) {
        this.documentProcessor = documentProcessor;
    }

    @Override
    public Object[] getNodes(Object node, String xpath)
            throws XPathEvaluationException {
        return documentProcessor.getNodes(node, xpath);
    }

    @Override
    public String getText(Object node, String xpath)
            throws XPathEvaluationException {
        return documentProcessor.getText(node, xpath);
    }

    @Override
    public Object parseDocument(String xmlText) throws Exception {
        return documentProcessor.parseDocument(xmlText);
    }

}