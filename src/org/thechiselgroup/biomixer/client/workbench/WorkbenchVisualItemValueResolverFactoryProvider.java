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
package org.thechiselgroup.biomixer.client.workbench;

import java.util.Date;

import org.thechiselgroup.choosel.core.client.ui.Color;
import org.thechiselgroup.choosel.core.client.util.DataType;
import org.thechiselgroup.choosel.core.client.util.math.AverageCalculation;
import org.thechiselgroup.choosel.core.client.util.math.MaxCalculation;
import org.thechiselgroup.choosel.core.client.util.math.MinCalculation;
import org.thechiselgroup.choosel.core.client.util.math.SumCalculation;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem.Subset;
import org.thechiselgroup.choosel.core.client.visualization.model.managed.DefaultVisualItemResolverFactoryProvider;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.ResourceCountResolver;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.VisualItemIdResolver;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.managed.CalculationResolverFactory;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.managed.FirstResourcePropertyResolverFactory;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.managed.FixedVisualItemResolverFactory;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.managed.SingletonVisualItemResolverFactory;

import com.google.inject.Inject;

public class WorkbenchVisualItemValueResolverFactoryProvider extends
        DefaultVisualItemResolverFactoryProvider {

    public static final SingletonVisualItemResolverFactory COUNT_RESOLVER_FACTORY = new SingletonVisualItemResolverFactory(
            "ResourceCountResolverFactory", DataType.NUMBER, "Count",
            new ResourceCountResolver(Subset.ALL));

    public static final SingletonVisualItemResolverFactory ID_RESOLVER_FACTORY = new SingletonVisualItemResolverFactory(
            "VisualItemStatusIdFactory", DataType.TEXT, "Group Name",
            new VisualItemIdResolver());

    public static final FirstResourcePropertyResolverFactory LOCATION_PROPERTY_RESOLVER_FACTORY = new FirstResourcePropertyResolverFactory(
            "Location-Property-Resolver", DataType.LOCATION);

    public static final FirstResourcePropertyResolverFactory DATE_PROPERTY_RESOLVER_FACTORY = new FirstResourcePropertyResolverFactory(
            "Date-Property-Resolver", DataType.DATE);

    public static final CalculationResolverFactory MIN_RESOLVER_FACTORY = new CalculationResolverFactory(
            "min", new MinCalculation());

    public static final CalculationResolverFactory MAX_RESOLVER_FACTORY = new CalculationResolverFactory(
            "max", new MaxCalculation());

    public static final CalculationResolverFactory AVERAGE_RESOLVER_FACTORY = new CalculationResolverFactory(
            "avg", new AverageCalculation());

    public static final CalculationResolverFactory SUM_RESOLVER_FACTORY = new CalculationResolverFactory(
            "sum", new SumCalculation());

    public static final FirstResourcePropertyResolverFactory TEXT_PROPERTY_RESOLVER_FACTORY = new FirstResourcePropertyResolverFactory(
            "Text-Property-Resolver", DataType.TEXT);

    public static final FixedVisualItemResolverFactory FIXED_TEXT_EMPTY_RESOLVER_FACTORY = new FixedVisualItemResolverFactory(
            "fixed_empty_string", DataType.TEXT, "(empty)");

    public static final FixedVisualItemResolverFactory FIXED_DATE_TODAY_RESOLVER_FACTORY = new FixedVisualItemResolverFactory(
            "fixed-date-today", DataType.DATE, new Date());

    public static final FixedVisualItemResolverFactory FIXED_COLOR_STEELBLUE_RESOLVER_FACTORY = new FixedVisualItemResolverFactory(
            "fixed-stdblue", DataType.COLOR, new Color(100, 149, 237));

    public static final FixedVisualItemResolverFactory FIXED_NUMBER_0_RESOLVER_FACTORY = new FixedVisualItemResolverFactory(
            "Fixed-0", DataType.NUMBER, new Double(0.0));

    public static final FixedVisualItemResolverFactory FIXED_NUMBER_1_RESOLVER_FACTORY = new FixedVisualItemResolverFactory(
            "Fixed-1", DataType.NUMBER, new Double(1.0));

    @Inject
    public void registerFactories() {
        register(ID_RESOLVER_FACTORY);

        // number specific resolvers
        register(COUNT_RESOLVER_FACTORY);
        register(SUM_RESOLVER_FACTORY);
        register(AVERAGE_RESOLVER_FACTORY);
        register(MAX_RESOLVER_FACTORY);
        register(MIN_RESOLVER_FACTORY);

        // first resource value resolvers
        register(TEXT_PROPERTY_RESOLVER_FACTORY);
        register(DATE_PROPERTY_RESOLVER_FACTORY);
        register(LOCATION_PROPERTY_RESOLVER_FACTORY);

        // fixed resolvers
        register(FIXED_NUMBER_0_RESOLVER_FACTORY);
        register(FIXED_NUMBER_1_RESOLVER_FACTORY);
        register(FIXED_COLOR_STEELBLUE_RESOLVER_FACTORY);
        register(FIXED_DATE_TODAY_RESOLVER_FACTORY);
        register(FIXED_TEXT_EMPTY_RESOLVER_FACTORY);
    }

}
