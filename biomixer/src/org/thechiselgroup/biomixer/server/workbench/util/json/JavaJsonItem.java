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

import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonArray;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonItem;

/**
 * NOTE: The results of the java-based JsonPath library's JsonPath.read(json,
 * path) is cast to whatever you assign it to. If you expect a JSONArray, you
 * put the type as List<Object>. If you expect a JSONObject, you put the type as
 * Object. Since we don't necessarily know whether a JsonItem is an array or
 * object, we always pass in an Object and then cast if necessary.
 * 
 * @author drusk
 * 
 */
public class JavaJsonItem implements JsonItem {

    private Object object;

    public JavaJsonItem(Object asObject) {
        this.object = asObject;
    }

    @Override
    public JsonArray asArray() {
        return new JavaJsonArray((List<Object>) object);
    }

    @Override
    public String stringValue() {
        return object.toString();
    }

}
