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
package org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed;

import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.VisualItemValueResolverFactory;

public abstract class AbstractVisualItemValueResolverFactory implements
        VisualItemValueResolverFactory {

    protected String id;

    protected DataType dataType;

    protected String label;

    public AbstractVisualItemValueResolverFactory(String id, DataType dataType,
            String label) {

        assert id != null;
        assert dataType != null;
        assert label != null;

        this.id = id;
        this.dataType = dataType;
        this.label = label;
    }

    /**
     * Override if required.
     * 
     * @return {@code true}, if slot data type equals required data type.
     */
    @Override
    public boolean canCreateApplicableResolver(Slot slot,
            LightweightCollection<VisualItem> visualItems) {

        assert slot != null;
        assert visualItems != null;

        return dataType.equals(slot.getDataType());
    }

    public DataType getDataType() {
        return dataType;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public final String getLabel() {
        return label;
    }

}