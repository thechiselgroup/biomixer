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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetTestUtils;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightList;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

public final class VisualItemTestUtils {

    public static Matcher<LightweightCollection<VisualItem>> containsVisualItemsForExactResourceSets(
            final ResourceSet... resourceSets) {

        return new TypeSafeMatcher<LightweightCollection<VisualItem>>() {
            @Override
            public void describeTo(Description description) {
                for (ResourceSet resourceSet : resourceSets) {
                    description.appendValue(resourceSet);
                }
            }

            @Override
            public boolean matchesSafely(LightweightCollection<VisualItem> set) {
                if (set.size() != resourceSets.length) {
                    return false;
                }

                for (ResourceSet resourceSet : resourceSets) {
                    boolean found = false;
                    for (VisualItem item : set) {
                        ResourceSet itemSet = item.getResources();

                        if (itemSet.size() == resourceSet.size()
                                && itemSet.containsAll(resourceSet)) {
                            found = true;
                        }
                    }

                    if (!found) {
                        return false;
                    }
                }

                return true;
            }
        };
    }

    public static VisualItem createVisualItem(int id) {
        return createVisualItem("" + id,
                ResourceSetTestUtils.createResources(id));
    }

    public static VisualItem createVisualItem(String visualItemId,
            ResourceSet resources) {

        final AtomicReference<Object> displayObjectBuffer = new AtomicReference<Object>();

        VisualItem visualItem = mock(VisualItem.class);

        when(visualItem.getResources()).thenReturn(resources);
        when(visualItem.getId()).thenReturn(visualItemId);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                displayObjectBuffer.set(invocation.getArguments()[0]);
                return null;
            }
        }).when(visualItem).setDisplayObject(any(Object.class));
        when(visualItem.getDisplayObject()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return displayObjectBuffer.get();
            }
        });

        return visualItem;
    }

    public static LightweightList<VisualItem> createVisualItems(
            int... visualItemId) {
        ResourceSet[] resourceSets = new ResourceSet[visualItemId.length];
        for (int i = 0; i < resourceSets.length; i++) {
            resourceSets[i] = ResourceSetTestUtils.toLabeledResourceSet(""
                    + visualItemId[i],
                    ResourceSetTestUtils.createResource(visualItemId[i]));
        }
        return VisualItemTestUtils.createVisualItems(resourceSets);
    }

    /**
     * Creates list of resource items with using the label of the resource sets
     * as group ids.
     */
    public static LightweightList<VisualItem> createVisualItems(
            ResourceSet... resourceSets) {

        LightweightList<VisualItem> resourceItems = CollectionFactory
                .createLightweightList();
        for (ResourceSet resourceSet : resourceSets) {
            resourceItems.add(VisualItemTestUtils.createVisualItem(
                    resourceSet.getLabel(), resourceSet));
        }

        return resourceItems;
    }

    private VisualItemTestUtils() {
    }

}