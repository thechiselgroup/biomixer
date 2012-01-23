/*******************************************************************************
 * Copyright 2012 David Rusk 
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
package org.thechiselgroup.biomixer.client.core.ui.widget.listbox;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;

public class ListBoxControlTest {

    @Mock
    private ListBoxPresenter presenter;

    private ListBoxControl<Integer> listBoxControl;

    private final Transformer<Integer, String> formatter = new Transformer<Integer, String>() {

        @Override
        public String transform(Integer value) throws Exception {
            return value.toString();
        }
    };

    @Test
    public void addOneItemShouldBeInValuesAndStringRepresentationInPresenter()
            throws Exception {
        listBoxControl.addItem(1);
        verify(presenter).addItem("1");
        assertThat(listBoxControl.getValues(),
                containsExactly(Arrays.asList(1)));
    }

    @Test
    public void addTwoItemsShouldBGeInValuesAndStringRepresentationInPresenter()
            throws Exception {
        listBoxControl.addItem(1);
        verify(presenter).addItem("1");
        listBoxControl.addItem(2);
        verify(presenter).addItem("2");
        assertThat(listBoxControl.getValues(),
                containsExactly(Arrays.asList(1, 2)));
    }

    @Test
    public void removeOneItemShouldNotBeInValuesOrPresenter() throws Exception {
        listBoxControl.addItem(1);
        listBoxControl.addItem(2);
        listBoxControl.removeItem(1);
        verify(presenter).removeItem("1");
        assertThat(listBoxControl.getValues(),
                containsExactly(Arrays.asList(2)));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.listBoxControl = new ListBoxControl<Integer>(presenter, formatter);
    }

    @Test
    public void setValuesShouldResetListAndAddStringValuesToPresenter() {
        listBoxControl.setValues(Arrays.asList(1, 2, 3));
        listBoxControl.setValues(Arrays.asList(4, 5, 6));
        assertThat(listBoxControl.getValues(),
                containsExactly(Arrays.asList(4, 5, 6)));
    }

    @Test
    public void setValuesShouldSetListAndAddStringValuesToPresenter() {
        listBoxControl.setValues(Arrays.asList(1, 2, 3));
        verify(presenter).addItem("1");
        verify(presenter).addItem("2");
        verify(presenter).addItem("3");
        assertThat(listBoxControl.getValues(),
                containsExactly(Arrays.asList(1, 2, 3)));
    }

}
