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
package org.thechiselgroup.biomixer.client.services.term;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractXMLWebResourceService;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TermServiceImplementation extends AbstractXMLWebResourceService
        implements TermServiceAsync {

    private final LightTermResponseWithoutRelationshipsParser responseParser;

    @Inject
    public TermServiceImplementation(UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            LightTermResponseWithoutRelationshipsParser responseParser) {

        super(urlFetchService, urlBuilderFactory);

        this.responseParser = responseParser;
    }

    protected String buildUrl(String ontologyId, String conceptFullId) {
        return urlBuilderFactory.createUrlBuilder()
                .path("bioportal/virtual/ontology/" + ontologyId)
                .parameter("light", "1").parameter("norelations", "1")
                .uriParameter("conceptid", conceptFullId).toString();
    }

    @Override
    public void getBasicInformation(final String ontologyId,
            String conceptFullId, final AsyncCallback<Resource> callback) {

        assert ontologyId != null;
        assert conceptFullId != null;
        assert callback != null;

        String url = buildUrl(ontologyId, conceptFullId);

        fetchUrl(callback, url, new Transformer<String, Resource>() {
            @Override
            public Resource transform(String value) throws Exception {
                return responseParser.parseConcept(ontologyId, value);
            }
        });
    }
}