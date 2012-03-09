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
package org.thechiselgroup.biomixer.client;

import java.util.List;
import java.util.Set;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.services.hierarchy.HierarchyPathServiceAsync;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyStatusServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.VerticalTreeLayoutAlgorithm;

import com.google.inject.Inject;

public class RootPathsLoader extends AbstractEmbedLoader {

    public static final String EMBED_MODE = "paths_to_root";

    @Inject
    private HierarchyPathServiceAsync hierarchyPathService;

    @Inject
    private TermServiceAsync termService;

    @Inject
    private ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService;

    @Inject
    private OntologyStatusServiceAsync ontologyStatusService;

    private void doLoadHierarchyData(final String shortConceptId) {

        hierarchyPathService.findHierarchyToRoot(virtualOntologyId,
                shortConceptId, new ErrorHandlingAsyncCallback<Set<String>>(
                        errorHandler) {

                    @Override
                    protected void runOnSuccess(Set<String> shortIdsInHierarchy)
                            throws Exception {

                        for (String shortId : shortIdsInHierarchy) {
                            loadConcept(shortId);
                        }
                    }

                    @Override
                    protected Throwable wrapException(Throwable caught) {
                        return new Exception(
                                "Could not retrieve hierarchy to root for "
                                        + shortConceptId, caught);
                    }
                });
    }

    @Override
    public String getEmbedMode() {
        return EMBED_MODE;
    }

    private void loadConcept(final String conceptShortId) {

        conceptNeighbourhoodService.getResourceWithRelations(virtualOntologyId,
                conceptShortId, new ErrorHandlingAsyncCallback<Resource>(
                        errorHandler) {

                    @Override
                    protected void runOnSuccess(Resource resource) {
                        graphView.getResourceModel().getAutomaticResourceSet()
                                .add(resource);
                        // TODO automatic layout re-execution on add?
                        layout(graphView);
                    }

                    @Override
                    protected Throwable wrapException(Throwable caught) {
                        return new Exception(
                                "Could not retrieve full term information for "
                                        + conceptShortId + " ontology "
                                        + virtualOntologyId, caught);
                    }
                });
    }

    @Override
    protected void loadData() {
        ontologyStatusService
                .getAvailableOntologies(new ErrorHandlingAsyncCallback<List<String>>(
                        errorHandler) {

                    @Override
                    protected void runOnSuccess(
                            List<String> availableVirtualOntologyIds)
                            throws Exception {

                        if (availableVirtualOntologyIds
                                .contains(virtualOntologyId)) {
                            loadUsingHierarchyService();
                        } else {
                            loadUsingRecursiveTermService();
                        }

                    }

                    @Override
                    protected Throwable wrapException(Throwable caught) {
                        return new Exception(
                                "Could not retrieve status information for ontologies",
                                caught);
                    }

                });

    }

    private void loadHierarchyData(final String shortConceptId) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                doLoadHierarchyData(shortConceptId);
            }
        }, new ViewIsReadyCondition(graphView), 50);
    }

    private void loadTerm(final String fullConceptId) {

        final String conceptUri = Concept.toConceptURI(virtualOntologyId,
                fullConceptId);
        if (graphView.getResourceModel().getResources()
                .containsResourceWithUri(conceptUri)) {
            return;
        }

        conceptNeighbourhoodService.getResourceWithRelations(virtualOntologyId,
                fullConceptId, new ErrorHandlingAsyncCallback<Resource>(
                        errorHandler) {

                    @Override
                    public void runOnSuccess(Resource resource) {
                        if (graphView.getResourceModel().getResources()
                                .containsResourceWithUri(conceptUri)) {
                            return;
                        }

                        graphView.getResourceModel().getAutomaticResourceSet()
                                .add(resource);
                        layout(graphView);

                        for (String parentUri : resource
                                .getUriListValue(Concept.PARENT_CONCEPTS)) {

                            String parentFullConceptId = Concept
                                    .getConceptId(parentUri);
                            loadTerm(parentFullConceptId);
                        }
                    }

                    @Override
                    protected Throwable wrapException(Throwable caught) {
                        return new Exception(
                                "Could not retrieve full term information for "
                                        + fullConceptId, caught);
                    }
                });
    }

    private void loadUsingHierarchyService() {

        // need to look up short id since that is what the hierarchy service
        // requires as a parameter
        termService.getBasicInformation(virtualOntologyId, fullConceptId,
                new ErrorHandlingAsyncCallback<Resource>(errorHandler) {

                    @Override
                    protected void runOnSuccess(Resource result)
                            throws Exception {

                        String shortId = (String) result
                                .getValue(Concept.SHORT_ID);
                        loadHierarchyData(shortId);
                    }

                    @Override
                    protected Throwable wrapException(Throwable caught) {
                        return new Exception(
                                "Could not retrieve basic information for "
                                        + fullConceptId, caught);
                    }

                });
    }

    private void loadUsingRecursiveTermService() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                loadTerm(fullConceptId);
            }
        }, new ViewIsReadyCondition(graphView), 50);
    }

    @Override
    protected void setLayoutAlgorithm() {
        this.layoutAlgorithm = new VerticalTreeLayoutAlgorithm(errorHandler);
    }

}
