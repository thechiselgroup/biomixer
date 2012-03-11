/*******************************************************************************
 * Copyright 2012 David Rusk, Lars Grammel 
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

import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.util.collections.IdentifiablesList;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

// TODO callback to change embeds...
public class TermCentricEmbedLoader implements EmbeddedViewLoader {

    private IdentifiablesList<TermEmbedLoader> embedLoaders = new IdentifiablesList<TermEmbedLoader>();

    /*
     * order is important, therefore we set all at once (alternative: producer)
     */
    @Inject
    public TermCentricEmbedLoader(PathsToRootEmbedLoader pathsToRootLoader,
            TermNeighbourhoodLoader termLoader,
            MappingNeighbourhoodLoader mappingLoader) {

        registerLoader(pathsToRootLoader);
        registerLoader(termLoader);
        registerLoader(mappingLoader);
    }

    @Override
    public Iterable<String> getEmbedModes() {
        return embedLoaders.getIds();
    }

    @Override
    public void loadView(WindowLocation windowLocation, String embedMode,
            AsyncCallback<View> callback) {

        assert embedLoaders.contains(embedMode);

        TermEmbedLoader termEmbedLoader = embedLoaders.get(embedMode);

        String fullConceptId = UriUtils.decodeURIComponent(windowLocation
                .getParameter("full_concept_id"));
        String virtualOntologyId = windowLocation
                .getParameter("virtual_ontology_id");

        termEmbedLoader.loadView(virtualOntologyId, fullConceptId, callback);
    }

    protected void registerLoader(TermEmbedLoader loader) {
        embedLoaders.add(loader);
    }

}