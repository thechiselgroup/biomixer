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

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.ui.Color;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.AbstractBasicVisualItemValueResolver;

// XXX introduce factory & UI factory
public class BioMixerNodeBackgroundColorResolver extends
        AbstractBasicVisualItemValueResolver {

    @Override
    public Object resolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {

        String type = Resource.getTypeFromURI(visualItem.getId());

        if (Concept.RESOURCE_URI_PREFIX.equals(type)) {
            return new Color("#DAE5F3");
        }

        if (Mapping.RESOURCE_URI_PREFIX.equals(type)) {
            return new Color("#E4E4E4");
        }

        if (Ontology.RESOURCE_URI_PREFIX.equals(type)) {
            return new Color("#000000");
        }

        // display unsupport elements in red
        return new Color("#ff0000");
    }
}