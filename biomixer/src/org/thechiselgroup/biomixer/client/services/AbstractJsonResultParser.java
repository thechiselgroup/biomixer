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

import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

public abstract class AbstractJsonResultParser implements JsonParser {

    private JsonParser jsonParser;

    public AbstractJsonResultParser(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    @Override
    public int asInt(Object intObject) {
        return jsonParser.asInt(intObject);
    }

    @Override
    public double asNumber(Object numberObject) {
        return jsonParser.asNumber(numberObject);
    }

    @Override
    public String asString(Object text) {
        return jsonParser.asString(text);
    }

    @Override
    public Object get(Object array, int index) {
        return jsonParser.get(array, index);
    }

    @Override
    public Object get(Object object, String property) {
        return jsonParser.get(object, property);
    }

    protected String getOntologyIdAsString(Object object, String property) {
        return Integer.toString(asInt(get(object, property)));
    }

    @Override
    public boolean has(Object object, String property) {
        return jsonParser.has(object, property);
    }

    @Override
    public boolean isArray(Object object) {
        return jsonParser.isArray(object);
    }

    @Override
    public int length(Object array) {
        return jsonParser.length(array);
    }

    @Override
    public Object parse(String json) {
        return jsonParser.parse(json);
    }

}
