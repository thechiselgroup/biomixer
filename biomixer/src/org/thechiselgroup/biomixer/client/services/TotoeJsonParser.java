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
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonItem;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

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
public class TotoeJsonParser extends AbstractJsonParser {

    public JSONArray getArray(String json, String path) {
        return JsonPath.select(parseJsonObject(json), path).isArray();
    }

    @Override
    // XXX better error handling?
    public JsonItem[] getJsonItems(String json, String path) {
        /*
         * Could either be retrieving a JSONObject directly, or a JSONArray. If
         * it is the array, return an array of JSONObject.
         */
        JSONValue startValue = JsonPath.select(parseJsonObject(json), path);
        JSONObject jsonObject = startValue.isObject();
        if (jsonObject != null) {
            return new JsonItem[] { new JsJsonItem(jsonObject) };
        }

        JSONArray jsonArray = startValue.isArray();
        if (jsonArray == null) {
            return new JsonItem[] {};
        }

        JsonItem[] jsonItems = new JsonItem[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = jsonArray.get(i).isObject();
            if (obj != null) {
                // XXX error
            }
            jsonItems[i] = new JsJsonItem(obj);
        }
        return jsonItems;
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
