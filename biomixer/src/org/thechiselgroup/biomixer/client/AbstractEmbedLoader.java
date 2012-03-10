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

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.visualization.DefaultView;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.dnd.windows.ViewWindowContent;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowContentProducer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutSupport;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;
import org.thechiselgroup.biomixer.shared.core.util.DelayedExecutor;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public abstract class AbstractEmbedLoader implements EmbeddedViewLoader {

    @Inject
    protected WindowContentProducer windowContentProducer;

    @Inject
    protected DelayedExecutor executor;

    @Inject
    protected ErrorHandler errorHandler;

    protected String fullConceptId;

    protected String virtualOntologyId;

    protected DefaultView graphView;

    protected LayoutAlgorithm layoutAlgorithm;

    protected void getDefaultView(AsyncCallback<View> callback) {
        final View graphView = ((ViewWindowContent) windowContentProducer
                .createWindowContent(Graph.ID)).getView();
        graphView.init();
        callback.onSuccess(graphView);
        this.graphView = (DefaultView) graphView;
    }

    protected void layout() {
        assert layoutAlgorithm != null;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                graphView.adaptTo(GraphLayoutSupport.class).runLayout(
                        layoutAlgorithm);
            }
        }, 50);
    }

    protected abstract void loadData();

    @Override
    public void loadView(WindowLocation windowLocation,
            AsyncCallback<View> callback) {
        getDefaultView(callback);
        retrieveUrlParameters(windowLocation);
        setLayoutAlgorithm();
        loadData();
    }

    protected void retrieveUrlParameters(WindowLocation windowLocation) {
        fullConceptId = UriUtils.decodeURIComponent(windowLocation
                .getParameter("full_concept_id"));
        virtualOntologyId = windowLocation.getParameter("virtual_ontology_id");
    }

    protected abstract void setLayoutAlgorithm();

}
