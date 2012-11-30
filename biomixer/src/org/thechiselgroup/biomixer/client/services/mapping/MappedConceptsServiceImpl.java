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
package org.thechiselgroup.biomixer.client.services.mapping;

import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Retrieves the concepts mapped to a given concept, as opposed to retrieving
 * the mapping resources like {@link ConceptMappingServiceAsync} does.
 * 
 * This service is a bit different than most in that it does not fetch data
 * directly from the server. Instead it calls other services in order to
 * retrieve the desired data.
 * 
 * @author drusk
 * 
 */
public class MappedConceptsServiceImpl implements MappedConceptsServiceAsync {

    @Inject
    private TermServiceAsync termService;

    @Inject
    private ConceptMappingServiceAsync mappingService;

    private final ErrorHandler errorHandler;

    @Inject
    public MappedConceptsServiceImpl(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public void getMappedConcepts(String ontologyId, String fullConceptId,
            AsyncCallback<List<Resource>> callback) {

        // TODO move calls from MappingNeighbourhoodLoader here

        // mappingService.getMappings(ontologyId, fullConceptId, new
        // ErrorHandlingAsyncCallback<List<Resource>>)

    }

}
