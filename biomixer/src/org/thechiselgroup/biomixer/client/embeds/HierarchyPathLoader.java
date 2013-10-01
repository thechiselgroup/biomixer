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
package org.thechiselgroup.biomixer.client.embeds;

import java.util.Set;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.services.hierarchy.HierarchyPathServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.shared.core.util.DelayedExecutor;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

/**
 * XXX Not currently in use. Uses hierarchy service (an approximately monthly
 * snapshot of path to root hierarchies). This allows the whole hierarchy to be
 * retrieved at once. However, this service is not currently supported via JSONP
 * so it has been removed from {@link PathsToRootEmbedLoader} to here in case it
 * is supported in the future.
 * 
 * @author drusk
 * 
 */
public class HierarchyPathLoader {

    @Inject
    private TermServiceAsync termService;

    @Inject
    private HierarchyPathServiceAsync hierarchyPathService;

    @Inject
    private ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService;

    private final ErrorHandler errorHandler;

    private final DelayedExecutor executor;

    public HierarchyPathLoader(ErrorHandler errorHandler,
            DelayedExecutor executor) {
        this.errorHandler = errorHandler;
        this.executor = executor;
    }

    private void doLoadHierarchyData(final String ontologyAcronym,
            final String shortConceptId, final View graphView) {

        hierarchyPathService
                .findHierarchyToRoot(ontologyAcronym, shortConceptId,
                        new TimeoutErrorHandlingAsyncCallback<Set<String>>(
                                errorHandler) {

                            @Override
                            protected String getMessage(Throwable caught) {
                                return "Could not retrieve hierarchy to root for "
                                        + shortConceptId;
                            }

                            @Override
                            protected void runOnSuccess(
                                    Set<String> shortIdsInHierarchy)
                                    throws Exception {

                                for (String shortId : shortIdsInHierarchy) {
                                    loadConcept(ontologyAcronym, shortId,
                                            graphView);
                                }
                            }

                        });
    }

    private void loadConcept(final String ontologyAcronym,
            final String conceptShortId, final View graphView) {

        conceptNeighbourhoodService.getResourceWithRelations(ontologyAcronym,
                conceptShortId,
                new TimeoutErrorHandlingAsyncCallback<Resource>(errorHandler) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return "Could not retrieve full term information for "
                                + conceptShortId + " ontology "
                                + ontologyAcronym;
                    }

                    @Override
                    protected void runOnSuccess(Resource resource) {
                        graphView.getResourceModel().getAutomaticResourceSet()
                                .add(resource);
                    }

                });
    }

    private void loadHierarchyData(final String ontologyAcronym,
            final String shortConceptId, final View graphView) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                doLoadHierarchyData(ontologyAcronym, shortConceptId,
                        graphView);
            }
        }, new ViewIsReadyCondition(graphView), 50);
    }

    public void loadUsingHierarchyService(final String ontologyAcronym,
            final String fullConceptId, final View graphView) {
        Window.alert("Moar alerts");
        // need to look up short id since that is what the hierarchy service
        // requires as a parameter
        termService.getBasicInformation(ontologyAcronym, fullConceptId,
                new TimeoutErrorHandlingAsyncCallback<Resource>(errorHandler) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return "Could not retrieve basic information for "
                                + fullConceptId;
                    }

                    @Override
                    protected void runOnSuccess(Resource result)
                            throws Exception {

                        String shortId = (String) result
                                .getValue(Concept.SHORT_ID);
                        loadHierarchyData(ontologyAcronym, shortId, graphView);
                    }

                });
    }

}
