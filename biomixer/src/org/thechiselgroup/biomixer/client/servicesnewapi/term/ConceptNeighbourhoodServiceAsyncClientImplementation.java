/*******************************************************************************
 * Copyright 2009, 2010, 2012 Lars Grammel, David Rusk 
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
package org.thechiselgroup.biomixer.client.servicesnewapi.term;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants;
import org.thechiselgroup.biomixer.client.core.error_handling.AsyncCallbackErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.embeds.TimeoutErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyNameServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author Lars Grammel
 * 
 * @see "http://www.bioontology.org/wiki/index.php/NCBO_REST_services#Term_services"
 */
public class ConceptNeighbourhoodServiceAsyncClientImplementation extends
        AbstractWebResourceService
        implements
        org.thechiselgroup.biomixer.client.servicesnewapi.term.ConceptNeighbourhoodServiceAsync {

    private final FullTermResponseJsonParser responseParser;

    private OntologyNameServiceAsync ontologyNameService;

    @Inject
    public ConceptNeighbourhoodServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            @Named(ChooselInjectionConstants.NEW_REST_API) UrlBuilderFactory urlBuilderFactory,
            // @Named(ChooselInjectionConstants.NEW_REST_API)
            OntologyNameServiceAsync ontologyNameService,
            FullTermResponseJsonParser responseParser) {

        super(urlFetchService, urlBuilderFactory);
        Window.alert("This too " + ontologyNameService.getClass());
        assert responseParser != null;
        assert ontologyNameService != null;

        this.responseParser = responseParser;
        this.ontologyNameService = ontologyNameService;
    }

    private String buildUrl(String fullConceptId, String ontologyAcronym) {
        return urlBuilderFactory.createUrlBuilder()
                .path("ontologies/" + ontologyAcronym + "/" + fullConceptId)
                .uriParameter("include", "all").toString();
    }

    @Override
    public void getNeighbourhood(final String ontologyAcroynm,
            final String conceptId,
            final AsyncCallback<ResourceNeighbourhood> callback) {

        assert ontologyAcroynm != null;
        assert conceptId != null;

        // Now this expects the URL based concept ID and ontology acronym
        final String url = buildUrl(conceptId, ontologyAcroynm);
        Window.alert("Here, good. New ConceptNeighbourhoodServicesAsync");
        ontologyNameService.getOntologyName(ontologyAcroynm,
                new TimeoutErrorHandlingAsyncCallback<String>(
                        new AsyncCallbackErrorHandler(callback)) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return "Could not retrieve concept neighbourhood for concept "
                                + conceptId + " in ontology " + ontologyAcroynm;
                    }

                    @Override
                    public void runOnSuccess(final String ontologyName) {
                        fetchUrl(
                                callback,
                                url,
                                new Transformer<String, ResourceNeighbourhood>() {
                                    @Override
                                    public ResourceNeighbourhood transform(
                                            String responseText)
                                            throws Exception {

                                        ResourceNeighbourhood neighbourhood = responseParser
                                                .parseNeighbourhood(
                                                        ontologyAcroynm,
                                                        responseText);

                                        for (Resource resource : neighbourhood
                                                .getResources()) {
                                            resource.putValue(
                                                    Concept.CONCEPT_ONTOLOGY_NAME,
                                                    ontologyName);
                                        }

                                        return neighbourhood;
                                    }
                                });
                    }

                });
    }

    @Override
    public void getResourceWithRelations(final String ontologyAcroynm,
            final String conceptId, final AsyncCallback<Resource> callback) {

        assert ontologyAcroynm != null;
        assert conceptId != null;

        // Now this expects the URL based concept ID and ontology acronym
        final String url = buildUrl(conceptId, ontologyAcroynm);

        ontologyNameService.getOntologyName(ontologyAcroynm,
                new TimeoutErrorHandlingAsyncCallback<String>(
                        new AsyncCallbackErrorHandler(callback)) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return "Could not retrieve concept neighbourhood for concept "
                                + conceptId + " in ontology " + ontologyAcroynm;
                    }

                    @Override
                    public void runOnSuccess(final String ontologyName) {
                        fetchUrl(callback, url,
                                new Transformer<String, Resource>() {
                                    @Override
                                    public Resource transform(
                                            String responseText)
                                            throws Exception {
                                        Resource resource = responseParser
                                                .parseResource(ontologyAcroynm,
                                                        responseText);
                                        resource.putValue(
                                                Concept.CONCEPT_ONTOLOGY_NAME,
                                                ontologyName);
                                        return resource;
                                    }
                                });
                    }

                });
    }

}