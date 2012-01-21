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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.thechiselgroup.biomixer.shared.core.test.matchers.collections.CollectionMatchers.containsExactly;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowableCaught;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowablesContainer;

public class ErrorListBoxFactoryTest {

    private final String errorMessage1 = "errorMessage";

    private final String errorMessage2 = "errorMessage2";

    private ThrowablesContainer throwablesContainer;

    private final ListBoxPresenter presenter = mock(ListBoxPresenter.class);

    @Test
    public void createErrorListBox() {
        ListBoxControl<ThrowableCaught> errorListBox = ErrorListBoxFactory
                .createErrorBox(throwablesContainer, presenter);

        ThrowableCaught throwableCaught = new ThrowableCaught(new Throwable(
                errorMessage1));
        throwablesContainer.addThrowableCaught(throwableCaught);
        verify(presenter).addItem(throwableCaught.toString());
    }

    @Test
    public void createErrorListBox2Errors() {
        ListBoxControl<ThrowableCaught> errorListBox = ErrorListBoxFactory
                .createErrorBox(throwablesContainer, presenter);

        throwablesContainer.addThrowableCaught(new ThrowableCaught(
                new Throwable(errorMessage1)));
        throwablesContainer.addThrowableCaught(new ThrowableCaught(
                new Throwable(errorMessage2)));

        assertThat(errorListBox.getValues(),
                containsExactly(throwablesContainer.getThrowablesCaught()));
    }

    @Before
    public void setUp() {
        throwablesContainer = new ThrowablesContainer();
    }
}
