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
package org.thechiselgroup.biomixer.client.workbench.error_handling;

import static org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingConstants.LOG;

import java.util.Collection;

import org.thechiselgroup.biomixer.client.core.command.AsyncCommandExecutor;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ExceptionUtil;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogManager;
import org.thechiselgroup.biomixer.client.workbench.feedback.FeedbackDialog;
import org.thechiselgroup.biomixer.client.workbench.feedback.FeedbackServiceAsync;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class FeedbackDialogErrorHandler implements ErrorHandler {

    private DialogManager dialogManager;

    private AsyncCommandExecutor executor;

    private FeedbackServiceAsync feedbackService;

    /*
     * INFO: We use the @Named(LOG) command executor to prevent infinite loops
     * when the feedback dialog throws errors.
     */
    @Inject
    public FeedbackDialogErrorHandler(DialogManager dialogManager,
            @Named(LOG) AsyncCommandExecutor executor,
            FeedbackServiceAsync feedbackService) {

        assert dialogManager != null;
        assert executor != null;
        assert feedbackService != null;

        this.executor = executor;
        this.feedbackService = feedbackService;
        this.dialogManager = dialogManager;
    }

    @Override
    public void handleError(Throwable error) {
        assert error != null;

        Collection<Throwable> causes = ExceptionUtil.getCauses(error);
        for (Throwable cause : causes) {
            dialogManager.show(new FeedbackDialog(cause, executor,
                    feedbackService));
        }
    }
}
