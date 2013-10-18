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
package org.thechiselgroup.biomixer.client.services.term;

import java.util.Map;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyNameServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;

import com.google.inject.Inject;

/**
 * @author Lars Grammel
 * 
 * @see "http://www.bioontology.org/wiki/index.php/NCBO_REST_services#Term_services"
 */
public class ConceptNeighbourhoodServiceAsyncClientImplementation extends
        AbstractWebResourceService implements ConceptNeighbourhoodServiceAsync {

    private final ConceptRelationshipJsonParser responseParser;

    @Inject
    public ConceptNeighbourhoodServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            OntologyNameServiceAsync ontologyNameService,
            ConceptRelationshipJsonParser responseParser) {

        super(urlFetchService, urlBuilderFactory);

        assert responseParser != null;
        assert ontologyNameService != null;

        this.responseParser = responseParser;
    }

    private class CompositionRelationFetcherCallback extends
            ErrorHandlingAsyncCallback<Map<String, String>> {

        private final ErrorHandlingAsyncCallback<ResourceNeighbourhood> completionCallback;

        private final String fullConceptId;

        private final String ontologyAcronym;

        private final Resource centralConcept;

        private CompositionRelationFetcherCallback(
                ErrorHandlingAsyncCallback<ResourceNeighbourhood> completionCallback,
                String fullConceptId, String ontologyAcronym,
                Resource centralConcept) {
            super(completionCallback.errorHandler);
            this.completionCallback = completionCallback;
            this.fullConceptId = fullConceptId;
            this.ontologyAcronym = ontologyAcronym;
            this.centralConcept = centralConcept;
        }

        @Override
        protected String getMessage(Throwable caught) {
            return "Could not expand composition neighbourhood for "
                    + fullConceptId;
        }

        @Override
        protected void runOnSuccess(Map<String, String> compositionNeighbourhood)
                throws Exception {

            // Gets the target relation with properties, parses properties for
            // composition relations,
            // and sends off calls to fetch the data for each of those. Those
            // can use the completionCallback on their completion.
            for (String neighbourConceptId : compositionNeighbourhood.keySet()) {
                final String compositionType = compositionNeighbourhood
                        .get(neighbourConceptId);
                // Add partial property
                if (Concept.HAS_PART_CONCEPTS.equals(compositionType)) {
                    centralConcept.addHasPart(Concept.toConceptURI(
                            ontologyAcronym, neighbourConceptId));
                } else if (Concept.PART_OF_CONCEPTS.equals(compositionType)) {
                    centralConcept.addPartOf(Concept.toConceptURI(
                            ontologyAcronym, neighbourConceptId));
                }

                String conceptUrl = buildBasicConceptUrl(neighbourConceptId,
                        ontologyAcronym);
                fetchUrl(completionCallback, conceptUrl,
                        new Transformer<String, ResourceNeighbourhood>() {
                            @Override
                            public ResourceNeighbourhood transform(
                                    String responseText) throws Exception {

                                // A neighbourhood; but for a single related
                                // concept.
                                ResourceNeighbourhood compositionNeighbour = responseParser
                                        .parseCompositionConceptAsNeighbourhood(
                                                ontologyAcronym,
                                                compositionType, responseText);

                                return compositionNeighbour;
                            }
                        });

            }
            return;
        }
    }

    private String buildBasicConceptUrl(String conceptFullId,
            String ontologyAcronym) {
        String encodedConceptId;
        if (conceptFullId.contains("http%3A")) {
            encodedConceptId = conceptFullId;
        } else {
            encodedConceptId = UriUtils.encodeURIComponent(conceptFullId);
        }
        // parents and children are direct subclass and direct superclass,
        // whereas ancestors and descendants include indirect.
        return urlBuilderFactory
                .createUrlBuilder()
                .path("ontologies/" + ontologyAcronym + "/classes/"
                        + encodedConceptId + "/").toString();
    }

    private String buildPathsToRootUrl(String conceptFullId,
            String ontologyAcronym) {
        String encodedConceptId;
        if (conceptFullId.contains("http%3A")) {
            encodedConceptId = conceptFullId;
        } else {
            encodedConceptId = UriUtils.encodeURIComponent(conceptFullId);
        }
        // parents and children are direct subclass and direct superclass,
        // whereas ancestors and descendants include indirect.
        return urlBuilderFactory
                .createUrlBuilder()
                .path("ontologies/" + ontologyAcronym + "/classes/"
                        + encodedConceptId + "/paths_to_root/").toString();
    }

    private String buildParentsUrl(String conceptFullId, String ontologyAcronym) {
        String encodedConceptId;
        if (conceptFullId.contains("http%3A")) {
            encodedConceptId = conceptFullId;
        } else {
            encodedConceptId = UriUtils.encodeURIComponent(conceptFullId);
        }
        // parents and children are direct subclass and direct superclass,
        // whereas ancestors and descendants include indirect.
        return urlBuilderFactory
                .createUrlBuilder()
                .path("ontologies/" + ontologyAcronym + "/classes/"
                        + encodedConceptId + "/parents/").toString();
    }

    private String buildCompositionUrl(String conceptFullId,
            String ontologyAcronym) {
        String encodedConceptId;
        if (conceptFullId.contains("http%3A")) {
            encodedConceptId = conceptFullId;
        } else {
            encodedConceptId = UriUtils.encodeURIComponent(conceptFullId);
        }
        // parents and children are direct subclass and direct superclass,
        // whereas ancestors and descendants include indirect.
        return urlBuilderFactory
                .createUrlBuilder()
                .path("ontologies/" + ontologyAcronym + "/classes/"
                        + encodedConceptId + "/")
                .parameter("include", "properties").toString();
    }

    private String buildChildrenUrl(String conceptFullId,
            String ontologyAcronym, int page) {
        String encodedConceptId;
        if (conceptFullId.contains("http%3A")) {
            encodedConceptId = conceptFullId;
        } else {
            encodedConceptId = UriUtils.encodeURIComponent(conceptFullId);
        }
        // parents and children are direct subclass and direct superclass,
        // whereas ancestors and descendants include indirect.
        return urlBuilderFactory
                .createUrlBuilder()
                .path("ontologies/" + ontologyAcronym + "/classes/"
                        + encodedConceptId + "/children/")
                .parameter("page", page + "").toString();
    }

    @Override
    public void getNeighbourhood(final String ontologyAcroynm,
            final String conceptId,
            final ErrorHandlingAsyncCallback<ResourceNeighbourhood> callback,
            Resource targetResource) {

        assert ontologyAcroynm != null;
        assert conceptId != null;

        // fetch parents
        final String parentsUrl = buildParentsUrl(conceptId, ontologyAcroynm);
        fetchUrl(callback, parentsUrl,
                new Transformer<String, ResourceNeighbourhood>() {
                    @Override
                    public ResourceNeighbourhood transform(String responseText)
                            throws Exception {

                        ResourceNeighbourhood neighbourhood = responseParser
                                .parseNewParents(ontologyAcroynm, responseText);

                        return neighbourhood;
                    }
                });

        // fetch children
        // url will be computed iteratively...
        // We need to deal with paged children data, and this is a tightly
        // contained way to do that.
        // This is a nice way to get closure in life :)
        (new Object() {
            private int pageNumberToRequest = 1;

            public void callForNextPage() {
                final String childrenUrl = buildChildrenUrl(conceptId,
                        ontologyAcroynm, pageNumberToRequest);

                fetchUrl(callback, childrenUrl,
                        new Transformer<String, ResourceNeighbourhood>() {
                            @Override
                            public ResourceNeighbourhood transform(
                                    String responseText) throws Exception {

                                ResourceNeighbourhood neighbourhood = responseParser
                                        .parseNewChildren(ontologyAcroynm,
                                                responseText);

                                // Before returning, check this response to see
                                // if we have more pages to get
                                Integer maxPageNumber = responseParser
                                        .asInt(responseParser.get(
                                                responseParser
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

                                return neighbourhood;
                            }
                        });
            }
        }).callForNextPage();


        // fetch composition relations (has_part and part_of)
        // The original callback can accept trickling" results, that is,
        // multiple runSuccess calls. Works great for the composition calls
        // that occur within the composition parser(should those be pulled out here?)
        final String compositionUrl = buildCompositionUrl(conceptId,
                ontologyAcroynm);
        CompositionRelationFetcherCallback compositionRelationReFetcherCallback = new CompositionRelationFetcherCallback(
                callback, conceptId, ontologyAcroynm, targetResource);
        fetchUrl(compositionRelationReFetcherCallback, compositionUrl,
                new Transformer<String, Map<String, String>>() {
                    @Override
                    public Map<String, String> transform(String responseText)
                            throws Exception {

                        Map<String, String> compositionNeighbours = responseParser
                                .parseForCompositionProperties(responseText);

                        return compositionNeighbours;
                    }
                });
    }

    @Override
    public void getPathToRootNeighbourhood(final String ontologyAcroynm,
            final String conceptId,
            final ErrorHandlingAsyncCallback<ResourceNeighbourhood> callback) {

        assert ontologyAcroynm != null;
        assert conceptId != null;

        // Now this expects the URL based concept ID and ontology acronym

        // final String url = buildNoRelationsUrl(conceptId, ontologyAcroynm);
        final String url = buildPathsToRootUrl(conceptId, ontologyAcroynm);
        // ontologyNameService.getOntologyName(ontologyAcroynm,
        // new ErrorHandlingAsyncCallback<String>(
        // new AsyncCallbackErrorHandler(callback)) {
        //
        // @Override
        // protected String getMessage(Throwable caught) {
        // return "Could not retrieve concept neighbourhood for concept "
        // + conceptId + " in ontology " + ontologyAcroynm;
        // }
        //
        // @Override
        // public void runOnSuccess(final String ontologyName) {
        fetchUrl(callback, url,
                new Transformer<String, ResourceNeighbourhood>() {
                    @Override
                    public ResourceNeighbourhood transform(String responseText)
                            throws Exception {
                        ResourceNeighbourhood pathToRoot = responseParser
                                .parseNewPathsToRoot(ontologyAcroynm,
                                        responseText);
                        // resource.putValue(
                        // Concept.CONCEPT_ONTOLOGY_NAME,
                        // ontologyName);
                        return pathToRoot;
                    }
                });
        // }
        //
        // });
    }

}