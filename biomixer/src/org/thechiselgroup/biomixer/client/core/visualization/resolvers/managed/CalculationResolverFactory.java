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
import org.thechiselgroup.biomixer.client.core.util.math.Calculation;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.CalculationResolver;

public class CalculationResolverFactory extends
        PropertyDependantVisualItemValueResolverFactory {

    private final Calculation calculation;

    public CalculationResolverFactory(String id, Calculation calculation) {
        super(id, DataType.NUMBER, calculation.toString());

        assert calculation != null;
        this.calculation = calculation;
    }

    @Override
    protected CalculationResolver createUnmanagedResolver(String property) {
        return new CalculationResolver(property, calculation);
    }

}