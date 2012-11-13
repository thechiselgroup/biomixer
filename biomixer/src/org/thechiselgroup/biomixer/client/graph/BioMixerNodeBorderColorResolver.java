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
package org.thechiselgroup.biomixer.client.graph;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.ui.Color;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.AbstractBasicVisualItemValueResolver;

// TODO factory, ui factory
public class BioMixerNodeBorderColorResolver extends
        AbstractBasicVisualItemValueResolver {

    @Override
    public Object resolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {
        String type = Resource.getTypeFromURI(visualItem.getId());

        if (Concept.RESOURCE_URI_PREFIX.equals(type)) {
            return new Color("#AFC6E5");
        }

        if (Mapping.RESOURCE_URI_PREFIX.equals(type)) {
            return new Color("#D4D4D4");
        }

        if (Ontology.RESOURCE_URI_PREFIX.equals(type)) {
            return new Color("#ffffff");
        }

        // display unsupport elements in red
        return new Color("#ff0000");
    }

}