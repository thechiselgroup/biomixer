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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.AbstractResourceSetTest;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Subset;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed.ManagedVisualItemValueResolverDecorator;

public class SubsetDelegatingValueResolverTest extends AbstractResourceSetTest {

    private SubsetDelegatingValueResolver underTest;

    @Mock
    private Slot slot;

    @Mock
    private VisualItem visualItem;

    @Mock
    private VisualItemValueResolverContext context;

    private Subset subset;

    @Test
    public void delegatingToManagedDecoratorOfSubsetVisualItemValueResolverUsesSubset() {
        Object expectedResult = new Object();

        SubsetVisualItemValueResolver delegateResolver = mock(SubsetVisualItemValueResolver.class);
        ManagedVisualItemValueResolverDecorator managedDecorator = new ManagedVisualItemValueResolverDecorator(
                "id", delegateResolver);
        when(context.getResolver(slot)).thenReturn(managedDecorator);
        when(delegateResolver.resolve(visualItem, context, subset)).thenReturn(
                expectedResult);

        Object result = underTest.resolve(visualItem, context);

        assertSame(expectedResult, result);
    }

    @Test
    public void delegatingToSubsetVisualItemValueResolverUsesSubset() {
        Object expectedResult = new Object();

        SubsetVisualItemValueResolver delegateResolver = mock(SubsetVisualItemValueResolver.class);
        when(context.getResolver(slot)).thenReturn(delegateResolver);
        when(delegateResolver.resolve(visualItem, context, subset)).thenReturn(
                expectedResult);

        Object result = underTest.resolve(visualItem, context);

        assertSame(expectedResult, result);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        subset = Subset.HIGHLIGHTED;
        underTest = new SubsetDelegatingValueResolver(slot, subset);
    }
}
