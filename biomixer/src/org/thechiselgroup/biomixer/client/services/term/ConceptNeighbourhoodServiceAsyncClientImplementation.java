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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyNameServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

/**
 * @author Lars Grammel
 * 
 * @see "http://www.bioontology.org/wiki/index.php/NCBO_REST_services#Term_services"
 */
public class ConceptNeighbourhoodServiceAsyncClientImplementation extends
        AbstractWebResourceService implements ConceptNeighbourhoodServiceAsync {

    private final FullTermResponseJsonParser responseParser;

    // private OntologyNameServiceAsync ontologyNameService;

    @Inject
    public ConceptNeighbourhoodServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            OntologyNameServiceAsync ontologyNameService,
            FullTermResponseJsonParser responseParser) {

        super(urlFetchService, urlBuilderFactory);

        assert responseParser != null;
        assert ontologyNameService != null;

        this.responseParser = responseParser;
        // this.ontologyNameService = ontologyNameService;
    }

    private class ConceptParentChildCollectorNeighbourhoodCallback extends
            ErrorHandlingAsyncCallback<ResourceNeighbourhood> {

        private ArrayList<ResourceNeighbourhood> neighbourhoods = new ArrayList<ResourceNeighbourhood>();

        private final ErrorHandlingAsyncCallback<ResourceNeighbourhood> completionCallback;

        private boolean sendingToCompletion = false;

        private final String fullConceptId;

        private ConceptParentChildCollectorNeighbourhoodCallback(
                ErrorHandlingAsyncCallback<ResourceNeighbourhood> completionCallback,
                String fullConceptId) {
            super(completionCallback.errorHandler);
            this.completionCallback = completionCallback;
            this.fullConceptId = fullConceptId;
        }

        @Override
        protected String getMessage(Throwable caught) {
            return "Could not expand concept neighbourhood for "
                    + fullConceptId;
        }

        @Override
        protected void runOnSuccess(ResourceNeighbourhood targetNeighbourhood)
                throws Exception {
            // Being a little lazy and assuming the caller only calls with one
            // child and one parent rest call.

            neighbourhoods.add(targetNeighbourhood);
            if (neighbourhoods.size() >= 2 && sendingToCompletion != true) {
                sendingToCompletion = true;

                Map<String, Serializable> combinedPartialProperties = CollectionFactory
                        .createStringMap();
                UriList compiledParentList = new UriList();
                UriList compiledChildList = new UriList();
                UriList compiledOwningList = new UriList();
                UriList compiledOwnerList = new UriList();

                Map<String, Serializable> partialsFirst = neighbourhoods.get(0)
                        .getPartialProperties();
                Map<String, Serializable> partialsSecond = neighbourhoods
                        .get(1).getPartialProperties();

                // Add first
                compiledParentList.addAll((UriList) partialsFirst
                        .get(Concept.PARENT_CONCEPTS));
                compiledChildList.addAll((UriList) partialsFirst
                        .get(Concept.CHILD_CONCEPTS));
                compiledOwningList.addAll((UriList) partialsFirst
                        .get(Concept.OWNED_CONCEPTS));
                compiledOwnerList.addAll((UriList) partialsFirst
                        .get(Concept.OWNING_CONCEPTS));

                // Add second
                compiledParentList.addAll((UriList) partialsSecond
                        .get(Concept.PARENT_CONCEPTS));
                compiledChildList.addAll((UriList) partialsSecond
                        .get(Concept.CHILD_CONCEPTS));
                compiledOwningList.addAll((UriList) partialsSecond
                        .get(Concept.OWNED_CONCEPTS));
                compiledOwnerList.addAll((UriList) partialsSecond
                        .get(Concept.OWNING_CONCEPTS));

                // Recollect our partial proeprties container
                combinedPartialProperties.put(Concept.PARENT_CONCEPTS,
                        compiledParentList);
                combinedPartialProperties
                        .put(Concept.CHILD_CONCEPTS, compiledChildList);
                combinedPartialProperties.put(Concept.OWNED_CONCEPTS,
                        compiledOwningList);
                combinedPartialProperties.put(Concept.OWNING_CONCEPTS,
                        compiledOwnerList);

                // Recollect our resources
                List<Resource> combinedResources = neighbourhoods.get(0).getResources();
                combinedResources.addAll(neighbourhoods.get(1).getResources());
                // Window.alert(partialsFirst.toString());
                ResourceNeighbourhood combined = new ResourceNeighbourhood(
                        combinedPartialProperties, combinedResources);

                completionCallback.onSuccess(combined);
            }
            return;

        }

    }

    @Deprecated
    private String buildNoRelationsUrl(String conceptFullId,
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
                        + encodedConceptId).toString();
    }

    private String buildPathsToRootUrl(String conceptFullId,
            String ontologyAcronym) {
        // return urlBuilderFactory.createUrlBuilder()
        // .path("ontologies/" + ontologyAcronym + "/" + fullConceptId)
        // .uriParameter("include", "all").toString();
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
        // return urlBuilderFactory.createUrlBuilder()
        // .path("ontologies/" + ontologyAcronym + "/" + fullConceptId)
        // .uriParameter("include", "all").toString();
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

    private String buildChildrenUrl(String conceptFullId, String ontologyAcronym) {
        // return urlBuilderFactory.createUrlBuilder()
        // .path("ontologies/" + ontologyAcronym + "/" + fullConceptId)
        // .uriParameter("include", "all").toString();
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
                        + encodedConceptId + "/children/").toString();
    }

    @Override
    public void getNeighbourhood(final String ontologyAcroynm,
            final String conceptId,
            // final AsyncCallback<ResourceNeighbourhood> callback) {
            final ErrorHandlingAsyncCallback<ResourceNeighbourhood> callback) {

        assert ontologyAcroynm != null;
        assert conceptId != null;

        // Now this expects the URL based concept ID and ontology acronym
        final String parentsUrl = buildParentsUrl(conceptId, ontologyAcroynm);
        final String childrenUrl = buildChildrenUrl(conceptId, ontologyAcroynm);

        ConceptParentChildCollectorNeighbourhoodCallback combiningCallback = new ConceptParentChildCollectorNeighbourhoodCallback(
                callback, conceptId);

        // The combining callback will handle the split in data across two
        // calls, and leave the original callback to perform its initially
        // designed functions.
        // ontologyNameService.getOntologyName(ontologyAcroynm,
        // new ErrorHandlingAsyncCallback<String>(
        // new AsyncCallbackErrorHandler(combiningCallback)) {
        //
        // @Override
        // protected String getMessage(Throwable caught) {
        // return "Could not retrieve concept neighbourhood for concept "
        // + conceptId + " in ontology " + ontologyAcroynm;
        // }
        //
        // @Override
        // public void runOnSuccess(final String ontologyName) {
        ErrorHandlingAsyncCallback<ResourceNeighbourhood> parentChildCollector = new ConceptParentChildCollectorNeighbourhoodCallback(
                callback, conceptId);

        // We call two fetch calls, using the same combining
        // callback. It will store the first returned result,
        // and combine that neighbourhood with the second, and
        // re-dispatch it off to the actual useful callback. I
        // wanted to do this rather than to expose the
        // requirement of two REST calls to the original
        // callback.

        // fetch parents
        fetchUrl(parentChildCollector, parentsUrl,
                new Transformer<String, ResourceNeighbourhood>() {
                    @Override
                    public ResourceNeighbourhood transform(String responseText)
                            throws Exception {

                        ResourceNeighbourhood neighbourhood = responseParser
                                .parseNewParents(ontologyAcroynm, responseText);

                        // for (Resource resource : neighbourhood
                        // .getResources()) {
                        // resource.putValue(
                        // Concept.CONCEPT_ONTOLOGY_NAME,
                        // ontologyName);
                        // }

                        return neighbourhood;
                    }
                });
        // fetch children
        fetchUrl(parentChildCollector, childrenUrl,
                new Transformer<String, ResourceNeighbourhood>() {
                    @Override
                    public ResourceNeighbourhood transform(String responseText)
                            throws Exception {

                        ResourceNeighbourhood neighbourhood = responseParser
                                .parseNewChildren(ontologyAcroynm, responseText);

                        // for (Resource resource : neighbourhood
                        // .getResources()) {
                        // resource.putValue(
                        // Concept.CONCEPT_ONTOLOGY_NAME,
                        // ontologyName);
                        // }

                        return neighbourhood;
                    }
                });
    }

    // });
    // }

    @Override
    public void getPathToRootNeighbourhood(final String ontologyAcroynm,
            final String conceptId,
            final ErrorHandlingAsyncCallback<ResourceNeighbourhood> callback) {

        assert ontologyAcroynm != null;
        assert conceptId != null;

        // Now this expects the URL based concept ID and ontology acronym
        Window.alert("Rename this, it is for hierarchical or root to path. Maybe use ancestors call to do it, maybe use tree call.");
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