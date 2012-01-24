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

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowableCaught;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowablesContainer;

public class ErrorListBoxFactoryTest {

    private final String errorMessage1 = "errorMessage";

    private final String errorMessage2 = "errorMessage2";

    private ThrowablesContainer throwablesContainer;

    @Mock
    private ListBoxPresenter presenter;

    @Mock
    private ErrorHandler errorHandler;

    private ListBoxControl<ThrowableCaught> underTest;

    @Test
    public void addedThrowablesCaughtBeforeErrorListBoxCreationShouldBeInValues() {
        createAndAddThrowableCaught(errorMessage1);
        createErrorBox();
        createAndAddThrowableCaught(errorMessage2);
        assertErrorBoxContainsAllCaughtThrowables();
    }

    @Test
    public void addedThrowablesCaughtShouldBeValuesOfErrorListBox() {
        createErrorBox();
        createAndAddThrowableCaught(errorMessage1);
        createAndAddThrowableCaught(errorMessage2);
        assertErrorBoxContainsAllCaughtThrowables();
    }

    private void assertErrorBoxContainsAllCaughtThrowables() {
        assertThat(underTest.getValues(),
                containsExactly(throwablesContainer.getThrowablesCaught()));
    }

    private ThrowableCaught createAndAddThrowableCaught(String errorMessage) {
        ThrowableCaught throwableCaught = createThrowableCaught(errorMessage,
                new Date());
        throwablesContainer.addThrowableCaught(throwableCaught);
        return throwableCaught;
    }

    private void createErrorBox() {
        this.underTest = ErrorListBoxFactory.createErrorBox(
                throwablesContainer, presenter, errorHandler);
    }

    private ThrowableCaught createThrowableCaught(String errorMessage, Date date) {
        return new ThrowableCaught(new Throwable(errorMessage), date);
    }

    @Test
    public void descriptionOfAddedThrowableShouldGetAddedToPresenter() {
        createErrorBox();

        ThrowableCaught throwableCaught = createThrowableCaught(errorMessage1,
                new GregorianCalendar(2012, 1, 23).getTime());

        throwablesContainer.addThrowableCaught(throwableCaught);
        verify(presenter).addItem("Thu Feb 23 00:00:00 2012: " + errorMessage1);
    }

    @Test
    public void removeThrowableCaughtShouldBeRemovedFromValuesOfErrorListBox() {
        createErrorBox();
        ThrowableCaught throwable = createAndAddThrowableCaught(errorMessage1);
        createAndAddThrowableCaught(errorMessage2);
        throwablesContainer.removeThrowableCaught(throwable);
        assertErrorBoxContainsAllCaughtThrowables();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        throwablesContainer = new ThrowablesContainer();
    }
}
