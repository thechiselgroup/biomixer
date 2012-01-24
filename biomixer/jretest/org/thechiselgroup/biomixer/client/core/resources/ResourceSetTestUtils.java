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
package org.thechiselgroup.biomixer.client.core.resources;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.isEmpty;

import org.junit.Assert;
import org.mockito.ArgumentCaptor;

public final class ResourceSetTestUtils {

    public static final String TYPE_1 = "type-1";

    public static final String TYPE_2 = "type-2";

    public static final String SET_LABEL = "label";

    public static final String NUMBER_PROPERTY_1 = "number-1";

    public static final String NUMBER_PROPERTY_2 = "number-2";

    public static final String TEXT_PROPERTY_1 = "text-1";

    public static final String TEXT_PROPERTY_2 = "text-2";

    public static void assertContainsResource(boolean expected,
            ResourceSet resourceSet, String resourceType, int resourceId) {

        Assert.assertEquals(expected, resourceSet.contains(ResourceSetTestUtils
                .createResource(resourceType, resourceId)));
    }

    public static ArgumentCaptor<ResourceSetChangedEvent> captureOnResourceSetChanged(
            int expectedInvocationCount,
            ResourceSetChangedEventHandler resourcesChangedHandler) {

        ArgumentCaptor<ResourceSetChangedEvent> argument = ArgumentCaptor
                .forClass(ResourceSetChangedEvent.class);

        verify(resourcesChangedHandler, times(expectedInvocationCount))
                .onResourceSetChanged(argument.capture());

        return argument;
    }

    public static ResourceSet createLabeledResources(int... indices) {
        return createLabeledResources(SET_LABEL, TYPE_1, indices);
    }

    public static ResourceSet createLabeledResources(String type,
            int... indices) {
        return createLabeledResources(SET_LABEL, type, indices);
    }

    public static ResourceSet createLabeledResources(String label, String type,
            int... indices) {

        ResourceSet resources = createResources(type, indices);
        resources.setLabel(label);
        return resources;
    }

    public static Resource createResource(int index) {
        return createResource(TYPE_1, index);
    }

    public static Resource createResource(String type, int index) {
        Resource r = new Resource(type + ":" + index);

        double value1 = Math.random() * 10;
        double value2 = Math.random() * 10;

        r.putValue(NUMBER_PROPERTY_1, value1);
        r.putValue(NUMBER_PROPERTY_2, value2);

        r.putValue(TEXT_PROPERTY_1, "t1 " + index);
        r.putValue(TEXT_PROPERTY_2, "t2 " + index);

        return r;
    }

    public static ResourceSet createResources(int... indices) {
        return createResources(TYPE_1, indices);
    }

    public static ResourceSet createResources(String type, int... indices) {
        DefaultResourceSet resources = new DefaultResourceSet();
        for (int i : indices) {
            resources.add(createResource(type, i));
        }
        return resources;
    }

    public static ResourceSet toLabeledResourceSet(Resource... resources) {
        return toLabeledResourceSet(SET_LABEL, resources);
    }

    public static ResourceSet toLabeledResourceSet(ResourceSet... resourceSets) {
        return toLabeledResourceSet(SET_LABEL, resourceSets);
    }

    public static ResourceSet toLabeledResourceSet(String label,
            Resource... resources) {

        ResourceSet result = toResourceSet(resources);
        result.setLabel(label);
        return result;
    }

    public static ResourceSet toLabeledResourceSet(String label,
            ResourceSet... resourceSets) {

        ResourceSet result = toResourceSet(resourceSets);
        result.setLabel(label);
        return result;
    }

    /*
     * we don't expose this method outside the test environment because it
     * relies on DefaultResourceSet.
     */
    public static ResourceSet toResourceSet(Resource... resources) {
        ResourceSet result = new DefaultResourceSet();
        for (Resource resource : resources) {
            result.add(resource);
        }
        return result;
    }

    /*
     * we don't expose this method outside the test environment because it
     * relies on DefaultResourceSet.
     */
    public static ResourceSet toResourceSet(ResourceSet... resourceSets) {
        ResourceSet result = new DefaultResourceSet();
        for (ResourceSet resources : resourceSets) {
            result.addAll(resources);
        }
        return result;
    }

    public static void verifyOnResourcesAdded(
            ResourceSet expectedAddedResources,
            ResourceSetChangedEventHandler handler) {

        ResourceSetChangedEvent event = captureOnResourceSetChanged(1, handler)
                .getValue();

        assertThat(event.getAddedResources(),
                containsExactly(expectedAddedResources));
        assertThat(event.getRemovedResources(), isEmpty(Resource.class));
    }

    public static void verifyOnResourcesRemoved(ResourceSet expectedResources,
            ResourceSetChangedEventHandler handler) {

        ResourceSetChangedEvent event = captureOnResourceSetChanged(1, handler)
                .getValue();

        assertThat(event.getAddedResources(), isEmpty(Resource.class));
        assertThat(event.getRemovedResources(),
                containsExactly(expectedResources));
    }

    private ResourceSetTestUtils() {
    }
}