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
import java.util.Collection;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchService;

// for the user study - caches results indefinitely 
public class CachedDocumentFetchService implements DocumentFetchService {

    private DocumentBuilderFactory domBuilderFactory;

    private URLFetchService fetchService;

    private final PersistenceManagerFactory pmf;

    public CachedDocumentFetchService(URLFetchService fetchService,
            PersistenceManagerFactory pmf,
            DocumentBuilderFactory domBuilderFactory) {

        this.fetchService = fetchService;
        this.pmf = pmf;
        this.domBuilderFactory = domBuilderFactory;
    }

    @Override
    public Document fetchXML(String urlAsString) throws IOException,
            SAXException, ParserConfigurationException {

        assert urlAsString != null;

        // try to get from cache
        PersistenceManager manager = getPersistenceManager();
        try {
            Query query = manager.newQuery(PersistentHttpResult.class,
                    "url == urlParam");
            query.declareParameters("String urlParam");

            Collection<PersistentHttpResult> cachedResults = (Collection<PersistentHttpResult>) query
                    .execute(urlAsString);

            if (!cachedResults.isEmpty()) {
                Blob resultBlob = cachedResults.iterator().next().getResult();
                return parseDocument(resultBlob.getBytes());
            }
        } finally {
            manager.close();
        }

        HTTPRequest request = new HTTPRequest(new URL(urlAsString),
                HTTPMethod.GET, withDeadline(10d).followRedirects()
                        .disallowTruncate());

        byte[] content = fetchService.fetch(request).getContent();

        store(urlAsString, content);

        return parseDocument(content);
    }

    private PersistenceManager getPersistenceManager() {
        return pmf.getPersistenceManager();
    }

    private Document parseDocument(byte[] bytes) throws SAXException,
            IOException, ParserConfigurationException {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        return domBuilderFactory.newDocumentBuilder().parse(stream);
    }

    private void store(String urlAsString, byte[] content) {
        PersistenceManager manager = getPersistenceManager();
        try {
            PersistentHttpResult permission = new PersistentHttpResult();
            permission.setFetchDate(new Date());
            permission.setUrl(urlAsString);
            permission.setResult(new Blob(content));
            manager.makePersistent(permission);
        } finally {
            manager.close();
        }
    }

}
