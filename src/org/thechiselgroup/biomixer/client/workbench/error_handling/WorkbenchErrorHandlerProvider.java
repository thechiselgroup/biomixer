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
package org.thechiselgroup.biomixer.client.workbench.error_handling;

import org.thechiselgroup.biomixer.client.core.error_handling.CompositeErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.LoggingErrorHandler;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class WorkbenchErrorHandlerProvider implements Provider<ErrorHandler> {

    @Inject
    private FeedbackDialogErrorHandler feedbackDialogErrorHandler;

    @Inject
    private LoggingErrorHandler loggingErrorHandler;

    @Override
    public ErrorHandler get() {
        CompositeErrorHandler composite = new CompositeErrorHandler();
        composite.add(loggingErrorHandler);
        composite.add(feedbackDialogErrorHandler);
        return composite;
    }

}
