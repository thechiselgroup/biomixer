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
package org.thechiselgroup.biomixer.client.services.search.ontology;

import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

/**
 * 
 * @author everbeek
 * 
 */
public class OntologySubmissionJsonParser extends AbstractJsonResultParser {

    private String ontologyAcronym;

    @Inject
    public OntologySubmissionJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    private OntologyLatestSubmissionDetails analyzeItem(Object jsonItem) {
        OntologyLatestSubmissionDetails submissionDetails = new OntologyLatestSubmissionDetails(
                ontologyAcronym);
        // Not used now, but leads to more details
        Object ontologyDetails = get(jsonItem, "ontology");
        submissionDetails.description = asString(get(jsonItem, "description"));
        submissionDetails.version = asString(get(jsonItem, "version"));
        submissionDetails.submissionId = asInt(get(jsonItem, "submissionId"));
        submissionDetails.latest = true;

        return submissionDetails;
    }

    @Override
    public OntologyLatestSubmissionDetails parse(String json) {
        Object jsonObject = super.parse(json);
        if (has(jsonObject, "status") && has(jsonObject, "errors")) {
            return null;
        }
        return analyzeItem(jsonObject);
    }

    public void setOntologyAcronym(String ontologyAcronym) {
        this.ontologyAcronym = ontologyAcronym;
    }

}
