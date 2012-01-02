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
package org.thechiselgroup.biomixer.client.core.visualization.model.implementation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections.toCollection;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedVisualItemValueResolver;

/**
 * Creates mock {@link VisualItemValueResolver}s.
 * 
 * @author Lars Grammel
 */
public final class VisualItemValueResolverTestUtils {

    public static Slot[] createSlots(DataType... dataTypes) {
        Slot[] slots = new Slot[dataTypes.length];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new Slot("slot" + i, "Slot " + i, dataTypes[i]);
        }
        return slots;
    }

    public static VisualItemValueResolver mockDelegatingResolver(Slot... slots) {
        VisualItemValueResolver delegatingResolver = mockResolverThatCanAlwaysResolve();
        when(delegatingResolver.getTargetSlots()).thenReturn(
                toCollection(slots));
        return delegatingResolver;
    }

    public static ManagedVisualItemValueResolver mockManagedResolver(String id) {
        ManagedVisualItemValueResolver resolver = mock(ManagedVisualItemValueResolver.class);
        when(resolver.getTargetSlots()).thenReturn(
                LightweightCollections.<Slot> emptyCollection());
        when(resolver.getId()).thenReturn(id);
        return resolver;
    }

    public static VisualItemValueResolver mockResolver() {
        VisualItemValueResolver resolver = mock(VisualItemValueResolver.class);
        when(resolver.getTargetSlots()).thenReturn(
                LightweightCollections.<Slot> emptyCollection());
        return resolver;
    }

    public static VisualItemValueResolver mockResolverThatCanAlwaysResolve() {
        VisualItemValueResolver resolver = mockResolver();
        whenCanResolve(resolver).thenReturn(true);
        return resolver;
    }

    public static VisualItemValueResolver mockResolverThatCanNeverResolve() {
        VisualItemValueResolver resolver = mockResolver();
        whenCanResolve(resolver).thenReturn(false);
        return resolver;
    }

    public static VisualItemValueResolver mockResolverThatCanResolveExactResourceSet(
            final ResourceSet resources) {

        VisualItemValueResolver resolver = mockResolver();
        whenCanResolve(resolver).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                VisualItem visualItem = (VisualItem) invocation.getArguments()[0];
                ResourceSet set = visualItem.getResources();

                return set.size() == resources.size()
                        && set.containsAll(resources);
            };
        });
        return resolver;
    }

    public static VisualItemValueResolver mockResolverThatCanResolveIfContainsResources(
            final ResourceSet resources) {
        VisualItemValueResolver resolver = mockResolver();
        whenCanResolve(resolver).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                VisualItem visualItem = (VisualItem) invocation.getArguments()[0];
                ResourceSet set = visualItem.getResources();

                return set.containsAll(resources);
            }
        });
        return resolver;
    }

    public static VisualItemValueResolver mockResolverThatCanResolveProperty(
            final String property) {

        VisualItemValueResolver resolver = mockResolver();
        whenCanResolve(resolver).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                VisualItem visualItem = (VisualItem) invocation.getArguments()[0];
                ResourceSet set = visualItem.getResources();

                return set.size() == 1
                        && set.getFirstElement().containsProperty(property);
            };
        });
        return resolver;
    }

    private static OngoingStubbing<Boolean> whenCanResolve(
            VisualItemValueResolver resolver) {

        return when(resolver.canResolve(any(VisualItem.class),
                any(VisualItemValueResolverContext.class)));
    }

    private VisualItemValueResolverTestUtils() {
    }
}
