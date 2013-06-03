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
package org.thechiselgroup.biomixer.client.servicesnewapi.ontology;

import org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class OntologyNameServiceAsyncClientImplementation extends
        AbstractWebResourceService implements OntologyNameServiceAsync {

    private OntologyNameJsonParser parser;

    @Inject
    public OntologyNameServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            @Named(ChooselInjectionConstants.NEW_REST_API) UrlBuilderFactory urlBuilderFactory,
            OntologyNameJsonParser parser) {
        super(urlFetchService, urlBuilderFactory);

        assert parser != null;
        this.parser = parser;
    }

    private String buildUrl(String ontologyAcronym) {
        Window.alert("Got here finally");
        return urlBuilderFactory.createUrlBuilder()
                .path("/ontologies/" + ontologyAcronym).toString();
    }

    @Override
    public void getOntologyName(String ontologyAcronym,
            AsyncCallback<String> callback) {

        String url = buildUrl(ontologyAcronym);
        fetchUrl(callback, url, new Transformer<String, String>() {
            @Override
            public String transform(String responseText) throws Exception {
                return parser.parse(responseText);
            }

        });
    }

}
