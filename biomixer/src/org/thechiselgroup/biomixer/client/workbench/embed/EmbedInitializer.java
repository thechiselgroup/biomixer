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

import java.util.HashMap;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.error_handling.LoggingErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.BrowserDetect;
import org.thechiselgroup.biomixer.client.workbench.init.ApplicationInitializer;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class EmbedInitializer implements ApplicationInitializer {

    public final static String EMBED_MODE_PARAMETER = "embed_mode";

    @Inject
    private WindowLocation windowLocation;

    @Inject
    private LoggingErrorHandler loggingErrorHandler;

    @Inject
    private EmbedContainer embedContainer;

    @Inject
    private BrowserDetect browserDetect;

    private Map<String, EmbeddedViewLoader> embedLoaders = new HashMap<String, EmbeddedViewLoader>();

    @Override
    public void init() throws Exception {
        if (!browserDetect.isValidBrowser()) {
            Window.alert("Your browser is not supported. "
                    + "Choosel supports Chrome >=4, Firefox >= 3.5 and Safari >= 5");
        }

        embedContainer.init();

        // initGlobalErrorHandler();
        // TODO needs different handler? --> yes, show on info label
        // if there is good error handling in choosel entry point we dont need
        // this

        loadEmbed(windowLocation.getParameter(EMBED_MODE_PARAMETER));
    }

    private void loadEmbed(String embedMode) {
        if (!embedLoaders.containsKey(embedMode)) {

            String allEmbeds = "";
            for (String key : embedLoaders.keySet()) {
                allEmbeds += " " + key;
            }

            embedContainer.setInfoText("Embed mode '" + embedMode
                    + "' is invalid." + allEmbeds);
            return;
        }

        EmbeddedViewLoader embeddedViewLoader = embedLoaders.get(embedMode);

        embedContainer.setInfoText("Loading...");
        embeddedViewLoader.loadView(windowLocation, embedMode,
                new AsyncCallback<IsWidget>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        loggingErrorHandler.handleError(caught);
                        embedContainer.setInfoText(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(IsWidget result) {
                        embedContainer.setWidget(result.asWidget());
                    }
                }, new EmbedLoader() {
                    @Override
                    public void switchMode(String embedMode) {
                        loadEmbed(embedMode);
                    }
                });
    }

    protected void registerLoader(EmbeddedViewLoader loader) {
        assert loader != null;
        for (String embedMode : loader.getEmbedModes()) {
            embedLoaders.put(embedMode, loader);
        }
    }

    @SuppressWarnings("unused")
    @Inject
    private void setStoredViewEmbedLoader(StoredViewEmbedLoader loader) {
        registerLoader(loader);
    }
}