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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowableCaught;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowablesContainer;

public class ErrorListBoxFactoryTest {

    private final String errorMessage1 = "errorMessage";

    private final String errorMessage2 = "errorMessage2";

    private ThrowablesContainer throwablesContainer;

    @Mock
    private ListBoxPresenter presenter;

    @Test
    public void addedThrowablesCaughtBeforeErrorListBoxCreationShouldBeInValues() {
        throwablesContainer
                .addThrowableCaught(createThrowableCaught(errorMessage1));
        ListBoxControl<ThrowableCaught> errorListBox = ErrorListBoxFactory
                .createErrorBox(throwablesContainer, presenter);
        throwablesContainer
                .addThrowableCaught(createThrowableCaught(errorMessage2));
        assertThat(errorListBox.getValues(),
                containsExactly(throwablesContainer.getThrowablesCaught()));
    }

    @Test
    public void addedThrowablesCaughtShouldBeValuesOfErrorListBox() {
        ListBoxControl<ThrowableCaught> errorListBox = ErrorListBoxFactory
                .createErrorBox(throwablesContainer, presenter);

        throwablesContainer
                .addThrowableCaught(createThrowableCaught(errorMessage1));
        throwablesContainer
                .addThrowableCaught(createThrowableCaught(errorMessage2));

        assertThat(errorListBox.getValues(),
                containsExactly(throwablesContainer.getThrowablesCaught()));
    }

    private ThrowableCaught createThrowableCaught(String errorMessage) {
        return new ThrowableCaught(new Throwable(errorMessage));
    }

    @Test
    public void descriptionOfAddedThrowableShouldGetAddedToPresenter() {
        ListBoxControl<ThrowableCaught> errorListBox = ErrorListBoxFactory
                .createErrorBox(throwablesContainer, presenter);

        ThrowableCaught throwableCaught = createThrowableCaught(errorMessage1);
        throwablesContainer.addThrowableCaught(throwableCaught);
        verify(presenter).addItem(throwableCaught.toString());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        throwablesContainer = new ThrowablesContainer();
    }
}
