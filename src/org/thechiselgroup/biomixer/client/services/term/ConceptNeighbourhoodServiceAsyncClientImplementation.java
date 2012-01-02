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
package org.thechiselgroup.biomixer.client.services.term;

import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractXMLWebResourceService;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * @author Lars Grammel
 * 
 * @see "http://www.bioontology.org/wiki/index.php/NCBO_REST_services#Term_services"
 */
public class ConceptNeighbourhoodServiceAsyncClientImplementation extends
        AbstractXMLWebResourceService implements
        ConceptNeighbourhoodServiceAsync {

    private final FullTermResponseParser responseParser;

    @Inject
    public ConceptNeighbourhoodServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            FullTermResponseParser responseParser) {

        super(urlFetchService, urlBuilderFactory);

        this.responseParser = responseParser;
    }

    private String buildUrl(String conceptId, String ontologyId) {
        UrlBuilder urlBuilder = urlBuilderFactory.createUrlBuilder();
        urlBuilder.setPath("bioportal/virtual/ontology/" + ontologyId);
        urlBuilder.setParameter("conceptid",
                UriUtils.encodeURIComponent(conceptId));
        return urlBuilder.buildString();

    }

    @Override
    public void getNeighbourhood(final String ontologyId, String conceptId,
            final AsyncCallback<ResourceNeighbourhood> callback) {

        assert ontologyId != null;
        assert conceptId != null;

        String url = buildUrl(conceptId, ontologyId);

        fetchUrl(callback, url,
                new Transformer<String, ResourceNeighbourhood>() {
                    @Override
                    public ResourceNeighbourhood transform(String xmlText)
                            throws Exception {
                        return responseParser.parse(ontologyId, xmlText);
                    }
                });
    }

}