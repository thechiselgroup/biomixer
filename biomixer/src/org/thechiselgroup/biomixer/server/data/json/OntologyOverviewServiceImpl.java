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
package org.thechiselgroup.biomixer.server.data.json;

import java.io.IOException;

import org.thechiselgroup.biomixer.client.services.ontology.overview.OntologyOverviewService;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class OntologyOverviewServiceImpl extends RemoteServiceServlet implements
        OntologyOverviewService {

    @Override
    public String getOntologyOverviewAsJson() throws IOException {
        return IOUtils.readIntoString(OntologyOverviewServiceImpl.class
                .getResourceAsStream("ontology_overview_data.json"));
    }

}
