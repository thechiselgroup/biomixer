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
package org.thechiselgroup.biomixer.client.services.hierarchy;

import java.util.Set;

import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class HierarchyPathServiceAsyncClientImplementation extends
        AbstractWebResourceService implements HierarchyPathServiceAsync {

    private final HierarchyParser parser;

    @Inject
    public HierarchyPathServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory, HierarchyParser parser) {
        super(urlFetchService, urlBuilderFactory);

        this.parser = parser;
    }

    private String buildUrl(String conceptId, String virtualOntologyId) {
        return urlBuilderFactory
                .createUrlBuilder()
                .path("/bioportal/virtual/rootpath/" + virtualOntologyId + "/"
                        + conceptId).toString();
    }

    @Override
    public void findHierarchyToRoot(final String virtualOntologyId,
            final String conceptId, AsyncCallback<Set<String>> callback) {

        String url = buildUrl(conceptId, virtualOntologyId);
        fetchUrl(callback, url, new Transformer<String, Set<String>>() {
            @Override
            public Set<String> transform(String xmlText) throws Exception {
                return parser.parse(conceptId, xmlText, virtualOntologyId);
            }

        });

    }

}
