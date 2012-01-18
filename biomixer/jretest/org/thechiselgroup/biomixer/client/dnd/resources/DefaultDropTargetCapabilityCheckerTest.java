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
package org.thechiselgroup.biomixer.client.dnd.resources;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetUtils;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;

public class DefaultDropTargetCapabilityCheckerTest {

    private DefaultDropTargetCapabilityChecker underTest;

    @Test
    public void dropNotPossibleIfRequiredDatePropertyNotAvailable() {
        ResourceSet resourceSet = new DefaultResourceSet();
        Resource resource = new Resource("test:1");
        resource.putValue("key1", "stringValue");
        resourceSet.add(resource);

        Slot[] slots = new Slot[] { new Slot("1", "slot1", DataType.DATE) };

        assertEquals(false, underTest.isValidDrop(slots, resourceSet));
    }

    @Test
    public void dropPossibleIfRequiredDatePropertyAvailable() {
        ResourceSet resourceSet = new DefaultResourceSet();
        Resource resource = new Resource("test:1");
        resource.putValue("key1", new Date());
        resourceSet.add(resource);

        Slot[] slots = new Slot[] { new Slot("1", "slot1", DataType.DATE) };

        assertEquals(true, underTest.isValidDrop(slots, resourceSet));
    }

    @Test
    public void dropPossibleIfRequiredLocationPropertyAvailable() {
        ResourceSet resourceSet = new DefaultResourceSet();

        Resource locationResource = new Resource("location:2");
        locationResource.putValue(ResourceSetUtils.LONGITUDE, 0d);
        locationResource.putValue(ResourceSetUtils.LATITUDE, 0d);

        Resource resource = new Resource("test:1");
        resource.putValue("key1", locationResource);
        resourceSet.add(resource);

        Slot[] slots = new Slot[] { new Slot("1", "slot1", DataType.LOCATION) };

        assertEquals(true, underTest.isValidDrop(slots, resourceSet));
    }

    @Before
    public void setUp() {
        underTest = new DefaultDropTargetCapabilityChecker();
    }

}