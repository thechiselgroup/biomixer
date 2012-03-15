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

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.util.collections.SingleItemIterable;
import org.thechiselgroup.biomixer.client.services.ontology.overview.OntologyOverviewServiceAsync;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbedLoader;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

/**
 * Calls the javascript implementation of the force directed ontologies overview
 * layout
 * 
 * @author drusk
 * 
 */
public class ForceDirectedOntologyOverviewLoader implements EmbeddedViewLoader {

    @Inject
    private ErrorHandler errorHandler;

    public static final String EMBED_MODE = "fd_oo";

    @Inject
    private OntologyOverviewServiceAsync ontologyOverviewService;

    private native void applyD3Layout(Element div, String json)/*-{
		$wnd.forceDirectedLayout(div, json);
    }-*/;

    @Override
    public Iterable<String> getEmbedModes() {
        return new SingleItemIterable<String>(EMBED_MODE);
    }

    @Override
    public void loadView(WindowLocation windowLocation, String embedMode,
            final AsyncCallback<IsWidget> callback, EmbedLoader loader) {

        // won't need WindowLocation for this embed because it just loads data
        // from a file on server

        // 1. get ontology data from file on server
        ontologyOverviewService
                .getOntologyOverviewAsJson(new ErrorHandlingAsyncCallback<String>(
                        errorHandler) {

                    @Override
                    protected void runOnSuccess(String json) {
                        // 2. call javascript code passing in json
                        // using label to get an empty div

                        Label label = new Label();
                        applyD3Layout(label.getElement(), json);
                        callback.onSuccess(label);
                    }

                });
    }
}
