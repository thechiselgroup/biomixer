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
package org.thechiselgroup.biomixer.client.core.visualization.resolvers;

import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Subset;

/**
 * {@link VisualItemValueResolver} that returns results for a specific
 * {@link Subset} of {@link VisualItem}. All such
 * {@link VisualItemValueResolver}s should subclass this class to enable partial
 * highlighting and selection.
 * 
 * @author Lars Grammel
 */
public abstract class SubsetVisualItemValueResolver extends
        AbstractBasicVisualItemValueResolver {

    private final Subset subset;

    public SubsetVisualItemValueResolver(Subset subset) {
        assert subset != null;
        this.subset = subset;
    }

    @Override
    public final Object resolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {

        return resolve(visualItem, context, subset);
    }

    /**
     * Resolves the view item value for the specified subset.
     */
    public abstract Object resolve(VisualItem visualItem,
            VisualItemValueResolverContext context, Subset subset);

}