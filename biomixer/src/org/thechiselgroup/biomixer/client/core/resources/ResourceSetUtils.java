/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.ui.Color;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

public final class ResourceSetUtils {

    public static final String LATITUDE = "latitude";

    public static final String LONGITUDE = "longitude";

    // XXX compare to others methods, remove code duplication
    public static LightweightList<String> getProperties(ResourceSet resources,
            DataType dataType) {

        return getPropertiesByDataType(resources).get(dataType);
    }

    public static DataTypeLists<String> getPropertiesByDataType(
            ResourceSet resourceSet) {

        if (resourceSet.isEmpty()) {
            return new DataTypeLists<String>();
        }

        // no aggregation
        DataTypeLists<String> result = new DataTypeLists<String>();
        Resource resource = resourceSet.getFirstElement();

        if (resource == null) {
            return result;
        }

        for (Entry<String, Serializable> entry : resource.getProperties()
                .entrySet()) {

            Serializable value = entry.getValue();
            String propertyName = entry.getKey();

            if (value instanceof String) {
                result.get(DataType.TEXT).add(propertyName);
            }
            if (value instanceof Double) {
                result.get(DataType.NUMBER).add(propertyName);
            }
            if (value instanceof Resource) {
                Resource r = (Resource) value;

                if (r.getValue(LATITUDE) != null
                        && r.getValue(LONGITUDE) != null) {

                    result.get(DataType.LOCATION).add(propertyName);
                }
            }
            if (value instanceof Date) {
                result.get(DataType.DATE).add(propertyName);
            }

        }

        return result;
    }

    // XXX why isn't this using the same code as getPropertiesByDataType?
    public static List<String> getPropertyNamesForDataType(
            ResourceSet resourceSet, DataType dataType) {

        if (resourceSet.isEmpty()) {
            return Collections.emptyList();
        }

        // no aggregation
        Resource resource = resourceSet.getFirstElement();
        List<String> properties = new ArrayList<String>();

        for (Entry<String, Serializable> entry : resource.getProperties()
                .entrySet()) {

            switch (dataType) {
            case TEXT: {
                if (entry.getValue() instanceof String) {
                    properties.add(entry.getKey());
                }
            }
                break;
            case NUMBER: {
                if (entry.getValue() instanceof Double) {
                    properties.add(entry.getKey());
                }
            }
                break;
            case LOCATION: {
                if (entry.getValue() instanceof Resource) {
                    Resource r = (Resource) entry.getValue();

                    if (r.getValue(LATITUDE) != null
                            && r.getValue(LONGITUDE) != null) {

                        properties.add(entry.getKey());
                    }
                }
            }
                break;
            case DATE: {
                if (entry.getValue() instanceof Date) {
                    properties.add(entry.getKey());
                }
            }
                break;
            case COLOR: {
                if (entry.getValue() instanceof Color) {
                    properties.add(entry.getKey());
                }
            }

            case SHAPE: {
                if (entry.getValue() instanceof String
                        && isShape(entry.getValue())) {
                    properties.add(entry.getKey());
                }
            }
            }
        }
        return properties;
    }

    // XXX VisualItems should not be referenced here.
    public static List<String> getSharedPropertiesOfDataType(
            LightweightCollection<VisualItem> visualItems, DataType dataType) {

        List<String> properties = new ArrayList<String>();

        if (visualItems.isEmpty()) {
            return properties;
        }

        // get all valid properties from the first visualItem
        for (String property : getProperties(visualItems.getFirstElement()
                .getResources(), dataType)) {
            properties.add(property);
        }

        // only keep properties that are shared by all of the resource
        for (VisualItem visualItem : visualItems) {
            ResourceSet resources = visualItem.getResources();
            properties.retainAll(getProperties(resources, dataType).toList());
        }

        return properties;
    }

    // XXX this method is a duplication of PVShape. We should probably extract
    // these variables into core at some point
    private static boolean isShape(Object value) {
        if (!(value instanceof String)) {
            return false;
        }
        // TODO these things are dependent on the Protovis stuff, and they
        // should be extracted into core, and implemented in Protovis
        return value.equals("cross") || value.equals("triangle")
                || value.equals("diamond") || value.equals("square")
                || value.equals("circle") || value.equals("tick")
                || value.equals("bar");
    }

    public static String[] toResourceIds(ResourceSet resources) {
        String[] resourceIds = new String[resources.size()];
        int i = 0;
        for (Resource resource : resources) {
            resourceIds[i++] = resource.getUri();
        }
        return resourceIds;
    }

    private ResourceSetUtils() {

    }

}
