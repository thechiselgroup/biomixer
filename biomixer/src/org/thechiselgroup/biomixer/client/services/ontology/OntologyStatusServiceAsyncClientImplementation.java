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
package org.thechiselgroup.biomixer.client.services.ontology;

import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractXMLWebResourceService;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class OntologyStatusServiceAsyncClientImplementation extends
        AbstractXMLWebResourceService implements OntologyStatusServiceAsync {

    public static final String AVAILABLE_STATUS = "28";

    private OntologyStatusParser parser;

    @Inject
    public OntologyStatusServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory, OntologyStatusParser parser) {
        super(urlFetchService, urlBuilderFactory);
        this.parser = parser;
    }

    private String buildUrl() {
        return urlBuilderFactory.createUrlBuilder().path("/obs/ontologies")
                .toString();
    }

    @Override
    public void getAvailableOntologies(AsyncCallback<List<String>> callback) {

        String url = buildUrl();
        fetchUrl(callback, url, new Transformer<String, List<String>>() {

            @Override
            public List<String> transform(String xmlText) throws Exception {
                Map<String, List<String>> virtualOntologyIdsByStatus = parser
                        .parseStatuses(xmlText);
                return virtualOntologyIdsByStatus.get(AVAILABLE_STATUS);
            }

        });

    }

}
