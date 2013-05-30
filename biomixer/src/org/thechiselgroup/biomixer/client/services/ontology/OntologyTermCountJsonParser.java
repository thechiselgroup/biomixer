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

import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

public class OntologyTermCountJsonParser extends AbstractJsonResultParser {

    @Inject
    public OntologyTermCountJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    /**
     * 
     * @param json
     *            input json
     * @return the number of terms in the ontology, in string value. Ex: "129"
     */
    @Override
    public String parse(String json) {
        return asString(get(
                get(get(get(get(super.parse(json), "success"), "data"), 0),
                        "page"), "numResultsTotal"));
    }
}
