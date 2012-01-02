/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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

import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.util.DataType;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem.Subset;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.FirstResourcePropertyResolver;

public class BioMixerGraphLabelResolver extends FirstResourcePropertyResolver {

    public BioMixerGraphLabelResolver() {
        super(Concept.LABEL, DataType.TEXT);
    }

    @Override
    public boolean canResolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {
        return true;
    }

    @Override
    public Object resolve(VisualItem visualItem,
            VisualItemValueResolverContext context, Subset subset) {

        // prevents mapping nodes from showing lable
        String type = Resource.getTypeFromURI(visualItem.getId());
        if (Concept.RESOURCE_URI_PREFIX.equals(type)) {
            return super.resolve(visualItem, context, subset);
        }
        return "";
    }
}