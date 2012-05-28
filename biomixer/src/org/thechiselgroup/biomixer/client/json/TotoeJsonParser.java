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
package org.thechiselgroup.biomixer.client.json;

import name.pehl.totoe.json.client.JsonPath;

import org.thechiselgroup.biomixer.server.workbench.util.json.JavaJsonParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonArray;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonItem;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
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

    @Override
    public JsonArray getArray(String json, String path) {
        JSONArray array = JsonPath.select(parseJsonObject(json), path)
                .isArray();
        return array == null ? null : new JsJsonArray(array);
    }

    @Override
    public JsonItem getItem(String json, String path) {
        return new JsJsonItem(JsonPath.select(parseJsonObject(json), path));
    }

    @Override
    public String getString(String json, String path) {
        JSONValue jsonValue = JsonPath.select(parseJsonObject(json), path);
        JSONString jsonString = jsonValue.isString();
        if (jsonString != null) {
            return jsonString.stringValue();
        }

        JSONNumber jsonNumber = jsonValue.isNumber();
        if (jsonNumber != null) {
            double doubleValue = jsonValue.isNumber().doubleValue();
            double rounded = Math.round(doubleValue);
            if (rounded == doubleValue) {
                return ("" + rounded).split("\\.")[0];
            } else {
                return "" + doubleValue;
            }
        }

        return null;
    }

    private JSONObject parseJsonObject(String json) {
        return JSONParser.parseStrict(json).isObject();
    }

}
