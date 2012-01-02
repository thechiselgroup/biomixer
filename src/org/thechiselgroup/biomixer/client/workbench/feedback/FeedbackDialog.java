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
package org.thechiselgroup.biomixer.client.workbench.feedback;

import org.thechiselgroup.biomixer.client.workbench.ui.dialog.AbstractDialog;
import org.thechiselgroup.choosel.core.client.command.AsyncCommand;
import org.thechiselgroup.choosel.core.client.command.AsyncCommandExecutor;
import org.thechiselgroup.choosel.core.client.util.HasDescription;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class FeedbackDialog extends AbstractDialog {

    private class SendFeedbackCommand implements AsyncCommand, HasDescription {

        @Override
        public void execute(AsyncCallback<Void> callback) {
            String message = error != null ? error.getMessage() : null;
            feedbackService.sendFeedback(commentArea.getText(), message,
                    callback);
        }

        @Override
        public String getDescription() {
            return "Sending feedback...";
        }
    }

    private static final String CSS_FEEDBACK_COMMENTS = "feedback-comments";

    private static final String CSS_FEEDBACK_MESSAGE = "feedback-message";

    private TextArea commentArea;

    private Throwable error;

    private AsyncCommandExecutor executor;

    private FeedbackServiceAsync feedbackService;

    private String message;

    private String title;

    private final String header;

    public FeedbackDialog(String title, String header, String message,
            Throwable error, AsyncCommandExecutor executor,
            FeedbackServiceAsync feedbackService) {

        assert title != null;
        assert message != null;
        assert executor != null;
        assert feedbackService != null;
        assert header != null;

        this.header = header;
        this.feedbackService = feedbackService;
        this.executor = executor;
        this.title = title;
        this.message = message;
        this.error = error;
    }

    public FeedbackDialog(Throwable error, AsyncCommandExecutor executor,
            FeedbackServiceAsync feedbackService) {
        this("Error", "An error occurred", error.getMessage() + "<br/></br>"
                + "<b>Please describe the circumstances of this error:</b>",
                error, executor, feedbackService);
    }

    @Override
    public void cancel() {
    }

    @Override
    public Widget getContent() {
        VerticalPanel panel = new VerticalPanel();

        HTML label = new HTML(message);
        label.addStyleName(CSS_FEEDBACK_MESSAGE);
        panel.add(label);

        commentArea = new TextArea();
        commentArea.setStyleName(CSS_FEEDBACK_COMMENTS);

        panel.add(commentArea);

        return panel;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public String getOkayButtonLabel() {
        return "Send";
    }

    @Override
    public String getWindowTitle() {
        return title;
    }

    @Override
    public void okay() {
        executor.execute(new SendFeedbackCommand());
    }
}