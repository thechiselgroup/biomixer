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
package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.biomixer.client.dnd.windows.ViewWindowContent;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowContentProducer;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutSupport;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphLayouts;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;
import org.thechiselgroup.choosel.core.client.error_handling.ErrorHandler;
import org.thechiselgroup.choosel.core.client.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.choosel.core.client.resources.DefaultResourceSet;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.ResourceSet;
import org.thechiselgroup.choosel.core.client.util.UriUtils;
import org.thechiselgroup.choosel.core.client.visualization.DefaultView;
import org.thechiselgroup.choosel.core.client.visualization.View;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class ConceptNeighbourhoodLoader implements EmbeddedViewLoader {

    public static final String EMBED_MODE = "concept_neighbourhood";

    @Inject
    private TermServiceAsync termService;

    @Inject
    private ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService;

    @Inject
    private WindowContentProducer windowContentProducer;

    @Inject
    private ErrorHandler errorHandler;

    private void doLoadData(final DefaultView view, final String ontologyId,
            final String conceptFullId) {

        termService.getBasicInformation(ontologyId, conceptFullId,
                new ErrorHandlingAsyncCallback<Resource>(errorHandler) {
                    @Override
                    protected void runOnSuccess(Resource result)
                            throws Exception {
                        ResourceSet resourceSet = new DefaultResourceSet();
                        resourceSet.add(result);
                        view.getResourceModel().addResourceSet(resourceSet);
                        layout(view);
                    };
                });

        conceptNeighbourhoodService.getNeighbourhood(ontologyId, conceptFullId,
                new ErrorHandlingAsyncCallback<ResourceNeighbourhood>(
                        errorHandler) {
                    @Override
                    protected void runOnSuccess(ResourceNeighbourhood result)
                            throws Exception {
                        ResourceSet resourceSet = new DefaultResourceSet();
                        resourceSet.addAll(result.getResources());
                        view.getResourceModel().addResourceSet(resourceSet);
                        layout(view);
                    };
                });
    }

    @Override
    public String getEmbedMode() {
        return EMBED_MODE;
    }

    private void layout(final DefaultView view) {
        new Timer() {
            @Override
            public void run() {
                view.adaptTo(GraphLayoutSupport.class).runLayout(
                        GraphLayouts.HORIZONTAL_TREE_LAYOUT);
            }
        }.schedule(50);
    }

    private void loadData(final DefaultView view, final String ontologyId,
            final String conceptFullId) {

        // XXX remove once proper view content display lifecycle is available
        if (view.isReady()) {
            doLoadData(view, ontologyId, conceptFullId);
        } else {
            new Timer() {
                @Override
                public void run() {
                    loadData(view, ontologyId, conceptFullId);
                }
            }.schedule(200);
        }
    }

    @Override
    public void loadView(WindowLocation windowLocation,
            AsyncCallback<View> callback) {

        final View graphView = ((ViewWindowContent) windowContentProducer
                .createWindowContent(Graph.ID)).getView();
        graphView.init();
        callback.onSuccess(graphView);

        String conceptFullId = windowLocation.getParameter("full_concept_id");
        conceptFullId = UriUtils.decodeURIComponent(conceptFullId);
        String ontologyId = windowLocation.getParameter("virtual_ontology_id");

        loadData((DefaultView) graphView, ontologyId, conceptFullId);

    }
}