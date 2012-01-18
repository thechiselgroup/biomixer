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

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.math.Calculation;
import org.thechiselgroup.biomixer.client.core.util.math.MathUtils;
import org.thechiselgroup.biomixer.client.core.util.math.NumberArray;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Subset;

public class CalculationResolver extends SubsetVisualItemValueResolver implements
        PropertyDependantVisualItemValueResolver {

    private final String property;

    private final Calculation calculation;

    public CalculationResolver(String property, Calculation calculation) {
        this(property, Subset.ALL, calculation);
    }

    public CalculationResolver(String property, Subset subset,
            Calculation calculation) {

        super(subset);

        assert property != null;
        assert calculation != null;

        this.property = property;
        this.calculation = calculation;
    }

    /*
     * TODO we should check to make sure that the resource's property is the
     * correct DataType. This should happen in the SlotMappingConfiguration once
     * Resolvers report their DataType.
     * 
     * TODO test (not existing property, invalid data type on property, all
     * okay)
     */
    @Override
    public boolean canResolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {

        assert visualItem != null;
        assert context != null;

        for (Resource resource : visualItem.getResources()) {
            // XXX also need to check property data type.
            if (!resource.containsProperty(property)
                    || resource.getValue(property) == null) {
                return false;
            }
        }

        return true;
    }

    public Calculation getCalculation() {
        return calculation;
    }

    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public Double resolve(VisualItem visualItem,
            VisualItemValueResolverContext context, Subset subset) {
        return calculation.calculate(toNumberArray(visualItem
                .getResources(subset)));
    }

    private NumberArray toNumberArray(LightweightCollection<Resource> resources) {
        NumberArray numberArray = MathUtils.createNumberArray();

        for (Resource resource : resources) {
            // TODO, what happens if this is null, should that be possible,
            // should it not show those resources
            numberArray.push((Double) resource.getValue(property));
        }

        return numberArray;
    }

    @Override
    public String toString() {
        return calculation.toString() + " " + property;
    }
}