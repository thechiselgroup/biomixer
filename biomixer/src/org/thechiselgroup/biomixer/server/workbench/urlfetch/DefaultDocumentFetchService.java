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
package org.thechiselgroup.biomixer.server.workbench.urlfetch;

import static com.google.appengine.api.urlfetch.FetchOptions.Builder.withDeadline;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;

public class DefaultDocumentFetchService implements DocumentFetchService {

    private static final double TEN_SECONDS = 10d;

    private DocumentBuilderFactory domBuilderFactory;

    private URLFetchService fetchService;

    public DefaultDocumentFetchService(URLFetchService fetchService,
            DocumentBuilderFactory domBuilderFactory) {

        assert fetchService != null;
        assert domBuilderFactory != null;

        this.fetchService = fetchService;
        this.domBuilderFactory = domBuilderFactory;
    }

    @Override
    public Document fetchXML(String urlAsString) throws IOException,
            SAXException, ParserConfigurationException {

        assert urlAsString != null;

        HTTPRequest request = new HTTPRequest(new URL(urlAsString),
                HTTPMethod.GET, withDeadline(TEN_SECONDS).followRedirects()
                        .disallowTruncate());

        HTTPResponse response = fetchService.fetch(request);

        ByteArrayInputStream stream = new ByteArrayInputStream(
                response.getContent());

        return domBuilderFactory.newDocumentBuilder().parse(stream);
    }

}
