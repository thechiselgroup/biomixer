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
package org.thechiselgroup.biomixer.client.core.visualization.model.implementation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.NUMBER_PROPERTY_1;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TEXT_PROPERTY_1;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.TEXT_PROPERTY_2;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.createResource;
import static org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils.toResourceSet;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.mockDelegatingResolver;
import static org.thechiselgroup.biomixer.client.core.visualization.model.implementation.VisualItemValueResolverTestUtils.mockResolverThatCanAlwaysResolve;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceByPropertyMultiCategorizer;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.math.AverageCalculation;
import org.thechiselgroup.biomixer.client.core.util.math.Calculation;
import org.thechiselgroup.biomixer.client.core.util.math.MaxCalculation;
import org.thechiselgroup.biomixer.client.core.util.math.MinCalculation;
import org.thechiselgroup.biomixer.client.core.util.math.SumCalculation;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.CalculationResolver;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.FirstResourcePropertyResolver;

public class DefaultVisualizationModelSlotMappingTest {

    private Slot textSlot;

    private Slot numberSlot;

    private DefaultVisualizationModel underTest;

    private DefaultVisualizationModelTestHelper helper;

    @Test
    public void averageCalculationOverGroup() {
        testCalculationOverGroup(4d, new AverageCalculation());
    }

    @Test
    public void changingSlotMappingUpdatesVisualItemValue1() {
        helper = new DefaultVisualizationModelTestHelper();
        helper.setSlots(textSlot);
        underTest = helper.createTestVisualizationModel();

        Resource resource = createResource(1);

        underTest.setResolver(textSlot, new FirstResourcePropertyResolver(
                TEXT_PROPERTY_1, DataType.TEXT));
        helper.getContainedResources().add(resource);
        underTest.setResolver(textSlot, new FirstResourcePropertyResolver(
                TEXT_PROPERTY_2, DataType.TEXT));

        assertEquals(resource.getValue(TEXT_PROPERTY_2), getFirstVisualItem()
                .getValue(textSlot));
    }

    @Test
    public void changingSlotMappingUpdatesVisualItemValue2() {
        underTest.setResolver(numberSlot, new CalculationResolver(
                NUMBER_PROPERTY_1, new SumCalculation()));
        getFirstVisualItem().getValue(numberSlot);
        underTest.setResolver(numberSlot, new CalculationResolver(
                NUMBER_PROPERTY_1, new MaxCalculation()));

        assertEquals(8d, getFirstVisualItem().getValue(numberSlot));
    }

    @Test
    public void changingSlotMappingUpdatesVisualItemValuesOfDependentSlots() {
        helper = new DefaultVisualizationModelTestHelper();
        Slot[] slots = helper.createSlots(DataType.TEXT, DataType.TEXT);
        underTest = helper.createTestVisualizationModel();

        Resource resource = createResource(1);

        VisualItemValueResolver delegatingResolver = mockDelegatingResolver(slots[1]);
        when(
                delegatingResolver.resolve(any(VisualItem.class),
                        any(VisualItemValueResolverContext.class))).thenReturn(
                "a1", "a2");

        underTest.setResolver(slots[0], delegatingResolver);
        underTest.setResolver(slots[1], mockResolverThatCanAlwaysResolve());

        underTest.getContentResourceSet().add(resource);
        getFirstVisualItem().getValue(slots[0]);

        underTest.setResolver(slots[1], mockResolverThatCanAlwaysResolve());

        assertThat(getFirstVisualItem().getValue(slots[0]), is((Object) "a2"));
    }

    private VisualItem getFirstVisualItem() {
        return underTest.getFullVisualItemContainer().getVisualItems()
                .getFirstElement();
    }

    @Test
    public void maxCalculationOverGroup() {
        testCalculationOverGroup(8d, new MaxCalculation());
    }

    @Test
    public void minCalculationOverGroup() {
        testCalculationOverGroup(0d, new MinCalculation());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        helper = new DefaultVisualizationModelTestHelper();
        helper.createSlots(DataType.TEXT, DataType.NUMBER);
        underTest = helper.createTestVisualizationModel();

        textSlot = helper.getSlots()[0];
        numberSlot = helper.getSlots()[1];

        Resource r1 = createResource(1);
        r1.putValue(NUMBER_PROPERTY_1, new Double(0));
        r1.putValue(TEXT_PROPERTY_1, "value2");

        Resource r2 = createResource(2);
        r2.putValue(NUMBER_PROPERTY_1, new Double(4));
        r2.putValue(TEXT_PROPERTY_1, "value2");

        Resource r3 = createResource(3);
        r3.putValue(NUMBER_PROPERTY_1, new Double(8));
        r3.putValue(TEXT_PROPERTY_1, "value2");

        helper.getContainedResources().addAll(toResourceSet(r1, r2, r3));
        underTest.setCategorizer(new ResourceByPropertyMultiCategorizer(
                TEXT_PROPERTY_1));
    }

    @Test
    public void sumCalculationOverGroup() {
        testCalculationOverGroup(12d, new SumCalculation());
    }

    private void testCalculationOverGroup(double expectedResult,
            Calculation calculation) {

        underTest.setResolver(numberSlot, new CalculationResolver(
                NUMBER_PROPERTY_1, calculation));

        List<VisualItem> resourceItems = underTest.getFullVisualItemContainer()
                .getVisualItems().toList();
        assertEquals(1, resourceItems.size());
        VisualItem resourceItem = resourceItems.get(0);
        assertEquals(expectedResult, resourceItem.getValue(numberSlot));
    }
}