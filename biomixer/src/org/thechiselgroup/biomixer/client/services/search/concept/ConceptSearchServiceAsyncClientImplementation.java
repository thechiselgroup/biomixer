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
package org.thechiselgroup.biomixer.client.services.search.concept;

import java.util.Set;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class ConceptSearchServiceAsyncClientImplementation extends
        AbstractWebResourceService implements ConceptSearchServiceAsync {

    private final ConceptSearchResultJsonParser resultParser;

    @Inject
    public ConceptSearchServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            ConceptSearchResultJsonParser resultParser) {

        super(urlFetchService, urlBuilderFactory);
        this.resultParser = resultParser;
    }

    private String buildUrl(String queryText, int pageNumber) {
        return urlBuilderFactory.createUrlBuilder().path("/search/")
                .uriParameter("q", queryText).parameter("isexactmatch", "true")
                .parameter("page", pageNumber + "").toString();
    }

    @Override
    public void searchConcept(final String queryText,
            final AsyncCallback<Set<Resource>> callback) {

        // This is a nice way to get closure in life :)
        (new Object() {
            private int pageNumberToRequest = 1;

            public void callForNextPage() {
                final String url = buildUrl(queryText, pageNumberToRequest);

                fetchUrl(callback, url,
                        new Transformer<String, Set<Resource>>() {
                            @Override
                            public Set<Resource> transform(String responseText)
                                    throws Exception {

                                Set<Resource> searchResultSubset = resultParser
                                        .parseSearchResults(responseText);

                                // Before returning, check this response to see
                                // if we have more pages to get
                                Integer maxPageNumber = resultParser
                                        .asInt(resultParser.get(resultParser
                                                .parse(responseText),
                                                "pageCount"));
                                // if (maxPageNumber > 1) {
                                // Window.alert(pageNumberToRequest + " of "
                                // + maxPageNumber + "");
                                // }
                                if (null != maxPageNumber
                                        && maxPageNumber > pageNumberToRequest) {
                                    pageNumberToRequest++;
                                    callForNextPage();
                                }

                                return searchResultSubset;
                            }
                        });
            }
        }).callForNextPage();
    }

}
