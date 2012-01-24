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

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowableCaught;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowableCaughtEvent;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowablesContainer;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowablesContainerEventListener;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormat;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormatFactory;

public class ErrorListBoxFactory {

    public static ListBoxControl<ThrowableCaught> createErrorBox(
            ThrowablesContainer throwablesContainer,
            ListBoxPresenter presenter,
            final DateTimeFormatFactory dateTimeFormatFactory,
            ErrorHandler errorHandler) {

        final ListBoxControl<ThrowableCaught> listBoxControl = new ListBoxControl<ThrowableCaught>(
                presenter, new Transformer<ThrowableCaught, String>() {
                    @Override
                    public String transform(ThrowableCaught throwableCaught) {
                        DateTimeFormat formatter = dateTimeFormatFactory
                                .createDateTimeFormat("E MMM dd HH:mm:ss yyyy");
                        return formatter.format(throwableCaught.getTimeStamp())
                                + ": "
                                + throwableCaught.getThrowable()
                                        .getLocalizedMessage();
                    }
                }, errorHandler, throwablesContainer.getThrowablesCaught());

        throwablesContainer.addListener(new ThrowablesContainerEventListener() {

            @Override
            public void onThrowableCaughtAdded(ThrowableCaughtEvent event) {
                listBoxControl.addItem(event.getThrowableCaught());
            }

            @Override
            public void onThrowableCaughtRemoved(ThrowableCaughtEvent event) {
                listBoxControl.removeItem(event.getThrowableCaught());
            }
        });

        return listBoxControl;
    }

}
