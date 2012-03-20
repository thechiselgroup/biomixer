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
package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.workbench.embed.EmbedInitializer;

import com.google.inject.Inject;

public class BioMixerEmbedInitializer extends EmbedInitializer {

    @SuppressWarnings("unused")
    @Inject
    private void setConceptNeighbourhoodLoader(TermCentricEmbedLoader loader) {
        registerLoader(loader);
    }

    @SuppressWarnings("unused")
    @Inject
    private void setJavascriptBasedVisualizationLoader(
            ForceDirectedOntologyOverviewLoader loader) {
        registerLoader(loader);
    }

    @SuppressWarnings("unused")
    @Inject
    private void setJavascriptBasedVisualizationLoader(MatrixLayoutLoader loader) {
        registerLoader(loader);
    }
}