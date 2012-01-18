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

import java.util.Set;

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionUtils;

public class ResourceByPropertyMultiCategorizer implements
        ResourceMultiCategorizer {

    private String property;

    public ResourceByPropertyMultiCategorizer(String property) {
        this.property = property;
    }

    @Override
    public boolean canCategorize(Resource resource) {
        Object value = resource.getValue(property);
        return value != null && value instanceof String;
    }

    @Override
    public Set<String> getCategories(Resource resource) {
        return CollectionUtils.toSet((String) resource.getValue(property));
    }

    public String getProperty() {
        return property;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
