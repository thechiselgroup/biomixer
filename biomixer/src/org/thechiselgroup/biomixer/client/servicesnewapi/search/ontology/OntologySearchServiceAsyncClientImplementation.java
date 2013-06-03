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
package org.thechiselgroup.biomixer.client.servicesnewapi.search.ontology;

import java.util.Collection;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;
import org.thechiselgroup.biomixer.client.servicesnewapi.ontology.OntologySearchResultJsonParser;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Differs from the original implementation in another package by the url
 * builder and the parser, both of which here are for the new API. The named
 * binding is temporary until we have fully switched over to the new API.
 * 
 */
public class OntologySearchServiceAsyncClientImplementation extends
        AbstractWebResourceService
        implements
        org.thechiselgroup.biomixer.client.servicesnewapi.search.ontology.OntologySearchServiceAsync {

    private final OntologySearchResultJsonParser resultParser;

    @Inject
    public OntologySearchServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            @Named(ChooselInjectionConstants.NEW_REST_API) UrlBuilderFactory urlBuilderFactory,
            OntologySearchResultJsonParser resultParserNewApi) {

        super(urlFetchService, urlBuilderFactory);

        this.resultParser = resultParserNewApi;
    }

    private String buildUrl(String queryText) {
        // http://stagedata.bioontology.org/ontologies?apikey=YourAPIKey
        // provides all ontologies.
        // We can do client side filtering on the returned ontology names,
        // rather than requesting a REST service that does so.
        // If we decide to, we can later request a REST service that filters the
        // results.
        return urlBuilderFactory.createUrlBuilder().path("/ontologies/")
                .toString();
    }

    @Override
    public void searchOntologies(final String queryText,
            final AsyncCallback<Set<Resource>> callback) {
        final String url = buildUrl(queryText);
        fetchUrl(callback, url, new Transformer<String, Set<Resource>>() {
            @Override
            public Set<Resource> transform(String responseText)
                    throws Exception {
                resultParser.setFilterPropertyAndContainedText(
                        Ontology.ONTOLOGY_FULL_NAME, queryText);
                return resultParser.parse(responseText);
            }
        });
    }

    @Override
    public void searchOntologiesPredeterminedSet(
            final Collection<String> virtualOntologyIds,
            final AsyncCallback<Set<Resource>> callback) {

        String url = buildUrl("");

        fetchUrl(callback, url, new Transformer<String, Set<Resource>>() {
            @Override
            public Set<Resource> transform(String responseText)
                    throws Exception {
                resultParser.setFilterPropertyAndContainedText(
                        Ontology.VIRTUAL_ONTOLOGY_ID, virtualOntologyIds);
                return resultParser.parse(responseText);
            }
        });

    }

}
