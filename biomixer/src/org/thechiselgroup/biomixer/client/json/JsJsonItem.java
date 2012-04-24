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

import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonArray;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonItem;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class JsJsonItem implements JsonItem {

    private JSONValue item;

    public JsJsonItem(JSONValue item) {
        this.item = item;
    }

    @Override
    public JsonArray asArray() {
        return new JsJsonArray(item.isArray());
    }

    @Override
    public String stringValue() {
        JSONObject asObject = item.isObject();
        if (asObject != null) {
            return asObject.toString();
        }

        JSONArray asArray = item.isArray();
        if (asArray != null) {
            return asArray.toString();
        }

        JSONString asString = item.isString();
        if (asString != null) {
            return asString.stringValue();
        }

        // TODO throw parse error?
        return null;
    }

}
