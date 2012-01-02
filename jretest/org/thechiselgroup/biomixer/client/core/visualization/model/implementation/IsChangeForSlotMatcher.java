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

import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.SlotMappingChangedEvent;

public class IsChangeForSlotMatcher extends
        TypeSafeMatcher<SlotMappingChangedEvent> {

    private final Slot slot;

    public IsChangeForSlotMatcher(Slot slot) {
        super(SlotMappingChangedEvent.class);
        this.slot = slot;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("change for").appendValue(slot);
    }

    @Override
    public boolean matchesSafely(SlotMappingChangedEvent event) {
        return event.getSlot() == slot;
    }

}