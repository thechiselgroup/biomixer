/*******************************************************************************
 * Copyright 2012 Lars Grammel
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

import java.util.Set;

import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class JsJsonParser implements JsonParser {

    @Override
    public Object getPossiblyMissing(Object object, String property) {
        if (has(object, property)) {
            try {
                // has() is failing, but I am in a hurry...try/catch then!
                return get(object, property);
            } catch (Throwable t) {
                return "";
            }
        }
        return "";

    }

    @Override
    public String getIntAsString(Object object, String property) {
        return Integer.toString(asInt(get(object, property)));
    }

    @Override
    public Integer asInt(Object jsonValue) {
        if (null != jsonValue) {
            return (int) ((JSONValue) jsonValue).isNumber().doubleValue();
        } else {
            return null;
        }
    }

    @Override
    public Double asNumber(Object jsonValue) {
        if (null != jsonValue) {
            return ((JSONValue) jsonValue).isNumber().doubleValue();
        } else {
            return null;
        }
    }

    @Override
    public String asString(Object jsonValue) {
        if (null == jsonValue) {
            return null;
        }
        JSONValue node = (JSONValue) jsonValue;

        if (node.isString() != null) {
            return node.isString().stringValue();
        }

        if (node.isNumber() != null) {
            return node.isNumber().toString();
        }

        return null;
    }

    @Override
    public Object get(Object jsonValue, int index) {
        JSONValue node = (JSONValue) jsonValue;

        if (node == null) {
            return null;
        }

        return node.isArray().get(index);
    }

    @Override
    public Object get(Object jsonValue, String property) {
        JSONValue node = (JSONValue) jsonValue;
        JSONObject object = node.isObject();
        return object != null ? object.get(property) : null;
    }

    @Override
    public Set<String> getObjectProperties(Object jsonValue) {
        JSONValue node = (JSONValue) jsonValue;
        JSONObject object = node.isObject();
        return object.keySet();
    }

    @Override
    public boolean has(Object jsonValue, String property) {
        return ((JSONValue) jsonValue).isObject().containsKey(property);
    }

    @Override
    public boolean isArray(Object jsonValue) {
        return ((JSONValue) jsonValue).isArray() != null;
    }

    @Override
    public int length(Object jsonValue) {
        return ((JSONValue) jsonValue).isArray().size();
    }

    @Override
    public Object parse(String json) {
        return JSONParser.parseStrict(json);
    }

}