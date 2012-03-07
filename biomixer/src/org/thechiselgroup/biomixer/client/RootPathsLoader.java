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

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.visualization.DefaultView;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.dnd.windows.ViewWindowContent;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowContentProducer;
import org.thechiselgroup.biomixer.client.services.hierarchy.HierarchyPathServiceAsync;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyStatusServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutSupport;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.vertical_tree.VerticalTreeLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;
import org.thechiselgroup.biomixer.shared.core.util.DelayedExecutor;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class RootPathsLoader implements EmbeddedViewLoader {

    public static final String EMBED_MODE = "paths_to_root";

    @Inject
    private HierarchyPathServiceAsync hierarchyPathService;

    @Inject
    private TermServiceAsync termService;

    @Inject
    private ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService;

    @Inject
    private WindowContentProducer windowContentProducer;

    @Inject
    private ErrorHandler errorHandler;

    @Inject
    private OntologyStatusServiceAsync ontologyStatusService;

    @Inject
    private DelayedExecutor executor;

    private void checkOntologyStatus(final View graphView,
            final String fullConceptId, final String virtualOntologyId) {

        ontologyStatusService
                .getAvailableOntologies(new ErrorHandlingAsyncCallback<List<String>>(
                        errorHandler) {

                    @Override
                    protected void runOnSuccess(
                            List<String> availableVirtualOntologyIds)
                            throws Exception {

                        if (availableVirtualOntologyIds
                                .contains(virtualOntologyId)) {
                            loadUsingHierarchyService((DefaultView) graphView,
                                    virtualOntologyId, fullConceptId);
                        } else {
                            loadUsingRecursiveTermService(
                                    (DefaultView) graphView, virtualOntologyId,
                                    fullConceptId);
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

    private void doLoadHierarchyData(final DefaultView view,
            final String virtualOntologyId, final String conceptId) {

        hierarchyPathService.findHierarchyToRoot(virtualOntologyId, conceptId,
                new ErrorHandlingAsyncCallback<Set<String>>(errorHandler) {

                    @Override
                    protected void runOnSuccess(Set<String> shortIdsInHierarchy)
                            throws Exception {

                        for (String shortId : shortIdsInHierarchy) {
                            loadConcept(view, virtualOntologyId, shortId);
                        }
                    }

                    @Override
                    protected Throwable wrapException(Throwable caught) {
                        return new Exception(
                                "Could not retrieve hierarchy to root for "
                                        + conceptId, caught);
                    }
                });
    }

    @Override
    public String getEmbedMode() {
        return EMBED_MODE;
    }

    private void layout(final DefaultView view) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                view.adaptTo(GraphLayoutSupport.class).runLayout(
                        new VerticalTreeLayoutAlgorithm(errorHandler));
            }
        }, 50);
    }

    private void loadConcept(final DefaultView view,
            final String virtualOntologyId, final String conceptShortId) {

        conceptNeighbourhoodService.getResourceWithRelations(virtualOntologyId,
                conceptShortId, new ErrorHandlingAsyncCallback<Resource>(
                        errorHandler) {

                    @Override
                    protected void runOnSuccess(Resource resource) {
                        view.getResourceModel().getAutomaticResourceSet()
                                .add(resource);
                        // TODO automatic layout re-execution on add?
                        layout(view);
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

    private void loadHierarchyData(final DefaultView view,
            final String virtualOntologyId, final String conceptId) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                doLoadHierarchyData(view, virtualOntologyId, conceptId);
            }
        }, new ViewIsReadyCondition(view), 50);
    }

    private void loadTerm(final String virtualOntologyId,
            final String fullConceptId, final DefaultView view) {

        final String conceptUri = Concept.toConceptURI(virtualOntologyId,
                fullConceptId);
        if (view.getResourceModel().getResources()
                .containsResourceWithUri(conceptUri)) {
            return;
        }

        conceptNeighbourhoodService.getResourceWithRelations(virtualOntologyId,
                fullConceptId, new ErrorHandlingAsyncCallback<Resource>(
                        errorHandler) {

                    @Override
                    public void runOnSuccess(Resource resource) {
                        if (view.getResourceModel().getResources()
                                .containsResourceWithUri(conceptUri)) {
                            return;
                        }

                        view.getResourceModel().getAutomaticResourceSet()
                                .add(resource);
                        layout(view);

                        for (String parentUri : resource
                                .getUriListValue(Concept.PARENT_CONCEPTS)) {

                            String parentFullConceptId = Concept
                                    .getConceptId(parentUri);
                            loadTerm(virtualOntologyId, parentFullConceptId,
                                    view);
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

    private void loadUsingHierarchyService(final DefaultView graphView,
            final String virtualOntologyId, final String fullConceptId) {

        // need to look up short id since that is what the hierarchy service
        // requires as a parameter
        termService.getBasicInformation(virtualOntologyId, fullConceptId,
                new ErrorHandlingAsyncCallback<Resource>(errorHandler) {

                    @Override
                    protected void runOnSuccess(Resource result)
                            throws Exception {

                        String shortId = (String) result
                                .getValue(Concept.SHORT_ID);
                        loadHierarchyData(graphView, virtualOntologyId, shortId);
                    }

                    @Override
                    protected Throwable wrapException(Throwable caught) {
                        return new Exception(
                                "Could not retrieve basic information for "
                                        + fullConceptId, caught);
                    }

                });
    }

    private void loadUsingRecursiveTermService(final DefaultView view,
            final String virtualOntologyId, final String fullConceptId) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                loadTerm(virtualOntologyId, fullConceptId, view);
            }
        }, new ViewIsReadyCondition(view), 50);
    }

    @Override
    public void loadView(WindowLocation windowLocation,
            AsyncCallback<View> callback) {
        final View graphView = ((ViewWindowContent) windowContentProducer
                .createWindowContent(Graph.ID)).getView();
        graphView.init();
        callback.onSuccess(graphView);

        final String fullConceptId = UriUtils.decodeURIComponent(windowLocation
                .getParameter("full_concept_id"));
        final String virtualOntologyId = windowLocation
                .getParameter("virtual_ontology_id");

        checkOntologyStatus(graphView, fullConceptId, virtualOntologyId);
    }

}
