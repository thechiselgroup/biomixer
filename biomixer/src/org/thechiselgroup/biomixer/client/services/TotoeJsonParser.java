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
package org.thechiselgroup.biomixer.client.services;

import name.pehl.totoe.json.client.JsonPath;

import org.thechiselgroup.biomixer.server.workbench.util.json.JavaJsonParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * Uses Totoe to provide JSONPath support in GWT. Totoe wraps Dojo's
 * (javascript) implementation of JSONPath, so this parser cannot be used in
 * regular unit tests.
 * 
 * See {@link JavaJsonParser} for a JSONPath parser that can be used in unit
 * tests.
 * 
 * @author drusk
 * 
 */
public class TotoeJsonParser implements JsonParser {

    public JSONArray getArray(String json, String path) {
        return JsonPath.select(parseJsonObject(json), path).isArray();
    }

    public JSONObject getObject(String json, String path) {
        return JsonPath.select(parseJsonObject(json), path).isObject();
    }

    @Override
    public String getString(String json, String path) {
        return JsonPath.select(parseJsonObject(json), path).isString()
                .stringValue();
    }

    private JSONObject parseJsonObject(String json) {
        return JSONParser.parseStrict(json).isObject();
    }

}
