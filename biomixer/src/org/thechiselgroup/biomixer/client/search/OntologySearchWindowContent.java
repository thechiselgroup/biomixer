/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.search;

import java.util.Set;

import org.thechiselgroup.biomixer.client.AbstractSearchWindowContent;
import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetFactory;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologySearchServiceAsync;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class OntologySearchWindowContent extends AbstractSearchWindowContent {

    private OntologySearchServiceAsync searchService;

    @Inject
    public OntologySearchWindowContent(
            ResourceSetFactory resourceSetFactory,
            @Named(ChooselInjectionConstants.NEW_REST_API) OntologySearchServiceAsync searchService,
            ViewWindowContentProducer viewFactory) {
        super(resourceSetFactory, Ontology.ONTOLOGY_FULL_NAME, viewFactory,
                OntologySearchCommand.NCBO_ONTOLOGY_SEARCH);
        this.searchService = searchService;
    }

    @Override
    protected void searchForTerm(String queryText,
            AsyncCallback<Set<Resource>> callBack) {
        searchService.searchOntologies(queryText, callBack);

    }
}