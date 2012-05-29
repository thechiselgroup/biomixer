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
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonItem;

import com.jayway.jsonpath.JsonPath;

/**
 * Uses a java-based implementation of JSONPath. This means it can be used in
 * regular unit tests, unlike {@link TotoeJsonParser}.
 * 
 * NOTE: JsonPath.read by default returns an Object, but casts the result to
 * whatever you try to assign it to.
 * 
 * NOTE2: If JsonPath.read cannot find any element matching the path, it results
 * in a ClassCastException: java.lang.String cannot be cast to java.util.Map
 * which is why we catch these exceptions and return null.
 * 
 * @author drusk
 * 
 */
public class JavaJsonParser extends AbstractJsonParser {

    @Override
    public JsonArray getArray(String json, String path) {
        try {
            List<Object> array = JsonPath.read(json, path);
            return new JavaJsonArray(array);
        } catch (ClassCastException classCastException) {
            // an array can not be found
            return null;
        }
    }

    @Override
    public JsonItem getItem(String json, String path) {
        try {
            Object read = JsonPath.read(json, path);
            return new JavaJsonItem(read);
        } catch (ClassCastException classCastException) {
            // the item could not be found
            return null;
        }
    }

    @Override
    public String getString(String json, String path) {
        Object read = JsonPath.read(json, path);
        return read == null ? null : read.toString();
    }

}
