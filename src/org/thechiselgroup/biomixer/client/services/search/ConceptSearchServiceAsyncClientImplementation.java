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
package org.thechiselgroup.biomixer.client.services.search;

import java.util.Set;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractXMLWebResourceService;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class ConceptSearchServiceAsyncClientImplementation extends
        AbstractXMLWebResourceService implements ConceptSearchServiceAsync {

    private final ConceptSearchResultParser resultParser;

    @Inject
    public ConceptSearchServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            ConceptSearchResultParser resultParser) {

        super(urlFetchService, urlBuilderFactory);

        this.resultParser = resultParser;
    }

    private String buildUrl(String queryText) {
        UrlBuilder urlBuilder = urlBuilderFactory.createUrlBuilder();
        urlBuilder.setPath("/bioportal/search/");
        urlBuilder.setParameter("query", UriUtils.encodeURIComponent(queryText));
        urlBuilder.setParameter("isexactmatch", "1");
        return urlBuilder.buildString();
    }

    @Override
    public void searchConcepts(String queryText,
            final AsyncCallback<Set<Resource>> callback) {

        String url = buildUrl(queryText);

        fetchUrl(callback, url, new Transformer<String, Set<Resource>>() {
            @Override
            public Set<Resource> transform(String xmlText) throws Exception {
                return resultParser.parse(xmlText);
            }
        });
    }

}
