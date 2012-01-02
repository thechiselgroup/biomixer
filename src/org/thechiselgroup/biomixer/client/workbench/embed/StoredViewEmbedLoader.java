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
package org.thechiselgroup.biomixer.client.workbench.embed;

import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;
import org.thechiselgroup.biomixer.client.workbench.init.WorkbenchInitializer;
import org.thechiselgroup.biomixer.client.workbench.services.AsyncCallbackDelegate;
import org.thechiselgroup.biomixer.client.workbench.workspace.ViewLoader;
import org.thechiselgroup.choosel.core.client.visualization.View;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class StoredViewEmbedLoader implements EmbeddedViewLoader {

    public static final String EMBED_MODE = "stored_view";

    @Inject
    private ViewLoader viewLoader;

    @Override
    public String getEmbedMode() {
        return EMBED_MODE;
    }

    @Override
    public void loadView(WindowLocation windowLocation,
            final AsyncCallback<View> callback) {

        String viewIdString = windowLocation
                .getParameter(WorkbenchInitializer.VIEW_ID);
        // TODO catch exception, handle in here
        final long viewId = Long.parseLong(viewIdString);

        viewLoader.loadView(viewId, new AsyncCallbackDelegate<View>(callback) {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(new Exception(
                        "Could not load visualization " + viewId + "."));
            }
        });
    }

}