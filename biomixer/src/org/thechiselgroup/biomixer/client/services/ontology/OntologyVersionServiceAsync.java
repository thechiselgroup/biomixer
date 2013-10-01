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
package org.thechiselgroup.biomixer.client.services.ontology;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface OntologyVersionServiceAsync {

    /*
     * A virtual ontology id always gets the latest version of the ontology.
     * This is used for most of the services such as concept neighbourhoods,
     * etc. However, some of the NCBO REST services such as finding the path to
     * root don't have an option for using a virtual ontology id. They must use
     * a specific ontology version id. This is the interface for converting a
     * virtual ontology id to a specific ontology version id.
     */
    void getOntologyVersionId(String ontologyAcronym,
            AsyncCallback<String> callback);

}
