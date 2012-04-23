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
package org.thechiselgroup.biomixer.server.workbench.util.json;

import java.util.List;

import org.thechiselgroup.biomixer.client.json.TotoeJsonParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonArray;

import com.jayway.jsonpath.JsonPath;

/**
 * Uses a java-based implementation of JSONPath. This means it can be used in
 * regular unit tests, unlike {@link TotoeJsonParser}.
 * 
 * @author drusk
 * 
 */
public class JavaJsonParser extends AbstractJsonParser {

    @Override
    public JsonArray getArray(String json, String path) {
        List<Object> array = JsonPath.read(json, path);
        return new JavaJsonArray(array);
    }

    @Override
    public String getString(String json, String path) {
        return JsonPath.read(json, path).toString();
    }

}
