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
package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.biomixer.client.core.resources.Resource;

public final class Mapping {

    public static final String RESOURCE_URI_PREFIX = "ncbo-mapping";

    public static final String ID = "id";

    /**
     * URI of the source concept of a mapping.
     */
    public static final String SOURCE = "source";

    /**
     * URI of the target concept of a mapping.
     */
    public static final String TARGET = "target";

    /**
     * The processes responsible for creating the mapping
     */
    public static final String PROCESSES = "mapping_processes";

    @Deprecated
    public static final String OLD_DATE = "date";

    @Deprecated
    public final static String OLD_MAPPING_TYPE = "mappingType";

    @Deprecated
    public final static String OLD_MAPPING_SOURCE = "mappingSource";

    @Deprecated
    public final static String OLD_MAPPING_SOURCE_NAME = "mappingSourceName";

    public static Resource createMappingResource(String mappingId,
            String sourceConceptUri, String targetConceptUri) {

        Resource mapping = Resource
                .createIndexedResource(toMappingURI(mappingId));
        mapping.putValue(ID, mappingId);
        mapping.putValue(SOURCE, sourceConceptUri);
        mapping.putValue(TARGET, targetConceptUri);
        return mapping;
    }

    public static String getSource(Resource mapping) {
        return (String) mapping.getValue(SOURCE);
    }

    public static String getTarget(Resource mapping) {
        return (String) mapping.getValue(TARGET);
    }

    public static boolean isMapping(Resource resource) {
        return resource.getUri().startsWith(RESOURCE_URI_PREFIX);
    }

    public static String toMappingURI(String mappingId) {
        return RESOURCE_URI_PREFIX + ":" + mappingId;
    }

    private Mapping() {
    }

}