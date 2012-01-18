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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.implementation.DefaultVisualItemResolutionErrorModel;

public class DefaultVisualItemResolutionErrorModelTest {

    private DefaultVisualItemResolutionErrorModel underTest;

    @Mock
    private VisualItem visualItem1;

    @Mock
    private VisualItem visualItem2;

    private Slot slot1;

    private Slot slot2;

    @Test
    public void clearSlotDoesNotRemoveVisualItemMarkIfOtherErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot2, visualItem1);
        underTest.clearErrors(slot1);

        assertThat(underTest.hasErrors(visualItem1), is(true));
    }

    @Test
    public void clearSlotRemovesSlotFromVisualItemErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot2, visualItem1);
        underTest.clearErrors(slot1);

        assertThat(underTest.getSlotsWithErrors(visualItem1),
                containsExactly(slot2));
    }

    @Test
    public void clearSlotRemovesSlotMark() {
        underTest.reportError(slot1, visualItem1);
        underTest.clearErrors(slot1);

        assertThat(underTest.hasErrors(slot1), is(false));
    }

    @Test
    public void clearSlotRemovesVisualItemMarkIfNoOtherErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.clearErrors(slot1);

        assertThat(underTest.hasErrors(visualItem1), is(false));
    }

    @Test
    public void clearVisualItemDoesNotRemovesSlotMarkIfOtherErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot1, visualItem2);
        underTest.clearErrors(visualItem1);

        assertThat(underTest.hasErrors(slot1), is(true));
    }

    @Test
    public void clearVisualItemRemovesSlotMarkIfNoOtherErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.clearErrors(visualItem1);

        assertThat(underTest.hasErrors(slot1), is(false));
    }

    @Test
    public void clearVisualItemRemovesVisualItemFromSlotErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot1, visualItem2);
        underTest.clearErrors(visualItem1);

        assertThat(underTest.getVisualItemsWithErrors(slot1),
                containsExactly(visualItem2));
    }

    @Test
    public void clearVisualItemRemovesVisualItemMark() {
        underTest.reportError(slot1, visualItem1);
        underTest.clearErrors(visualItem1);

        assertThat(underTest.hasErrors(visualItem1), is(false));
    }

    @Test
    public void getSlotsWithErrorsReturnEmptyListIfVisualItemIsErrorFree() {
        assertThat(underTest.getSlotsWithErrors(visualItem1).isEmpty(),
                is(true));
    }

    @Test
    public void getVisualItemsWithErrorsReturnEmptyListIfSlotIsErrorFree() {
        assertThat(underTest.getVisualItemsWithErrors(slot1).isEmpty(),
                is(true));
    }

    @Test
    public void initiallyErrorFree() {
        assertThat(underTest.hasErrors(), is(false));
    }

    @Test
    public void removeDoesNotRemoveSlotItemMarkIfOtherErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot1, visualItem2);
        underTest.removeError(slot1, visualItem1);

        assertThat(underTest.hasErrors(slot1), is(true));
    }

    @Test
    public void removeDoesNotRemoveVisualItemMarkIfOtherErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot2, visualItem1);
        underTest.removeError(slot1, visualItem1);

        assertThat(underTest.hasErrors(visualItem1), is(true));
    }

    @Test
    public void removeRemovesSlotFromVisualItemErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot2, visualItem1);
        underTest.removeError(slot1, visualItem1);

        assertThat(underTest.getSlotsWithErrors(visualItem1),
                containsExactly(slot2));
    }

    @Test
    public void removeRemovesSlotItemMarkIfNoOtherErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.removeError(slot1, visualItem1);

        assertThat(underTest.hasErrors(slot1), is(false));
    }

    @Test
    public void removeRemovesSlotMark() {
        underTest.reportError(slot1, visualItem1);
        underTest.removeError(slot1, visualItem1);

        assertThat(underTest.hasErrors(slot1), is(false));
    }

    @Test
    public void removeRemovesVisualItemFromSlotErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot1, visualItem2);
        underTest.removeError(slot1, visualItem1);

        assertThat(underTest.getVisualItemsWithErrors(slot1),
                containsExactly(visualItem2));
    }

    @Test
    public void removeRemovesVisualItemMark() {
        underTest.reportError(slot1, visualItem1);
        underTest.removeError(slot1, visualItem1);

        assertThat(underTest.hasErrors(visualItem1), is(false));
    }

    @Test
    public void removeRemovesVisualItemMarkIfNoOtherErrors() {
        underTest.reportError(slot1, visualItem1);
        underTest.removeError(slot1, visualItem1);

        assertThat(underTest.hasErrors(visualItem1), is(false));
    }

    @Test
    public void reportErrorAddsSlotToErrorSlots() {
        underTest.reportError(slot1, visualItem1);

        assertThat(underTest.getSlotsWithErrors(), containsExactly(slot1));
    }

    @Test
    public void reportErrorAddsSlotToVisualItemErrors() {
        underTest.reportError(slot1, visualItem1);

        assertThat(underTest.getSlotsWithErrors(visualItem1),
                containsExactly(slot1));
    }

    @Test
    public void reportErrorAddsVisualItemToErrorVisualItems() {
        underTest.reportError(slot1, visualItem1);

        assertThat(underTest.getVisualItemsWithErrors(slot1),
                containsExactly(visualItem1));
    }

    @Test
    public void reportErrorAddsVisualItemToVisualItemErrors() {
        underTest.reportError(slot1, visualItem1);

        assertThat(underTest.getVisualItemsWithErrors(),
                containsExactly(visualItem1));
    }

    @Test
    public void reportErrorMarksSlotAsErroneous() {
        underTest.reportError(slot1, visualItem1);

        assertThat(underTest.hasErrors(slot1), is(true));
    }

    @Test
    public void reportErrorMarksVisualItemAsErroneous() {
        underTest.reportError(slot1, visualItem1);

        assertThat(underTest.hasErrors(visualItem1), is(true));
    }

    @Test
    public void reportErrorSetsHasErrors() {
        underTest.reportError(slot1, visualItem1);

        assertThat(underTest.hasErrors(), is(true));
    }

    @Test
    public void reportErrorTwiceAddsSlotToErrorSlotsOnce() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot1, visualItem1);

        assertThat(underTest.getSlotsWithErrors(), containsExactly(slot1));
    }

    @Test
    public void reportErrorTwiceAddsSlotToVisualItemErrorsOnce() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot1, visualItem1);

        assertThat(underTest.getSlotsWithErrors(visualItem1),
                containsExactly(slot1));
    }

    @Test
    public void reportErrorTwiceAddsVisualItemToErrorVisualItemsOnce() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot1, visualItem1);

        assertThat(underTest.getVisualItemsWithErrors(slot1),
                containsExactly(visualItem1));
    }

    @Test
    public void reportErrorTwiceAddsVisualItemToVisualItemErrorsOnce() {
        underTest.reportError(slot1, visualItem1);
        underTest.reportError(slot1, visualItem1);

        assertThat(underTest.getVisualItemsWithErrors(),
                containsExactly(visualItem1));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        slot1 = new Slot("s1", "", DataType.TEXT);
        slot2 = new Slot("s2", "", DataType.TEXT);

        when(visualItem1.getId()).thenReturn("v1");
        when(visualItem2.getId()).thenReturn("v2");

        underTest = new DefaultVisualItemResolutionErrorModel();
    }
}