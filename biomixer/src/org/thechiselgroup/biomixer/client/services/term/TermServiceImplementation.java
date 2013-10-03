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
import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyNameServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TermServiceImplementation extends AbstractWebResourceService
        implements TermServiceAsync {

    private final TermWithoutRelationshipsJsonParser responseParser;

    private OntologyNameServiceAsync ontologyNameService;

    @Inject
    public TermServiceImplementation(UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            OntologyNameServiceAsync ontologyNameService,
            TermWithoutRelationshipsJsonParser responseParser) {

        super(urlFetchService, urlBuilderFactory);
        this.responseParser = responseParser;
        this.ontologyNameService = ontologyNameService;
    }

    protected String buildUrl(String ontologyAcronym, String conceptFullId) {
        String encodedConceptId;
        if (conceptFullId.contains("http%3A")) {
            encodedConceptId = conceptFullId;
        } else {
            encodedConceptId = UriUtils.encodeURIComponent(conceptFullId);
        }
        return urlBuilderFactory
                .createUrlBuilder()
                .path("/ontologies/" + ontologyAcronym + "/classes/"
                        + encodedConceptId).toString();
    }


    @Override
    public void getBasicInformation(final String ontologyAcronym,
            String conceptFullId, final AsyncCallback<Resource> callback) {
        assert ontologyAcronym != null;
        assert conceptFullId != null;
        assert callback != null;

        final String url = buildUrl(ontologyAcronym, conceptFullId);

        // TODO Do we actually need the ontology name, or would the ontology
        // acronym be sufficient?
        // ontologyNameService.getOntologyName(ontologyAcronym,
        // new ErrorHandlingAsyncCallback<String>(
        // new AsyncCallbackErrorHandler(callback)) {
        //
        // @Override
        // protected String getMessage(Throwable caught) {
        // return "Could not retrieve ontology name for ontology acronym: "
        // + ontologyAcronym;
        // }
        //
        // @Override
        // public void runOnSuccess(final String ontologyName) {
        fetchUrl(callback, url, new Transformer<String, Resource>() {
            @Override
            public Resource transform(String value) throws Exception {
                Resource resource = responseParser.parseConcept(
                        ontologyAcronym, value);

                // resource.putValue(
                // Concept.CONCEPT_ONTOLOGY_NAME,
                // ontologyName);
                // resource.putValue(
                // Concept.ONTOLOGY_ACRONYM,
                // ontologyAcronym);
                return resource;
            }
        });
        // }
        //
        // });
    }

}