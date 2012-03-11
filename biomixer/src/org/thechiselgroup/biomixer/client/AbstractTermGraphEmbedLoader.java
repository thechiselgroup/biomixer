/*******************************************************************************
 * Copyright 2012 Lars Grammel 
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
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.dnd.windows.ViewWindowContent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;
import org.thechiselgroup.biomixer.shared.core.util.DelayedExecutor;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class AbstractTermGraphEmbedLoader implements TermEmbedLoader {

    @Named("embed")
    @Inject
    protected ViewWindowContentProducer viewContentProducer;

    @Inject
    protected DelayedExecutor executor;

    @Inject
    protected ErrorHandler errorHandler;

    private final String id;

    private final String label;

    public AbstractTermGraphEmbedLoader(String label, String id) {
        assert label != null;
        assert id != null;

        this.id = id;
        this.label = label;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final String getLabel() {
        return label;
    }

    protected abstract void loadData(String virtualOntologyId,
            String fullConceptId, View graphView);

    @Override
    public final void loadView(String virtualOntologyId, String fullConceptId,
            AsyncCallback<View> callback) {

        View graphView = ((ViewWindowContent) viewContentProducer
                .createWindowContent(Graph.ID)).getView();
        graphView.init();
        callback.onSuccess(graphView);

        loadData(virtualOntologyId, fullConceptId, graphView);
    }
}
