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
package org.thechiselgroup.biomixer.client.core.visualization.resolvers;

import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;

// TODO expose data type
public class FixedValueResolver extends AbstractBasicVisualItemValueResolver {

    private final Object value;

    private final DataType dataType;

    public FixedValueResolver(Object value, DataType dataType) {
        assert value != null;
        assert dataType != null;

        this.value = value;
        this.dataType = dataType;
    }

    @Override
    public boolean canResolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {

        return true;
    }

    @Override
    public Object resolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {
        return value;
    }

    @Override
    public String toString() {
        return "Constant: " + value;
    }

}