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
package org.thechiselgroup.biomixer.server.workbench.feedback;

import org.thechiselgroup.biomixer.client.core.util.ServiceException;
import org.thechiselgroup.biomixer.client.core.util.task.Task;
import org.thechiselgroup.biomixer.client.workbench.feedback.FeedbackService;
import org.thechiselgroup.biomixer.server.workbench.server.ChooselServiceServlet;

import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.users.UserServiceFactory;

public class FeedbackServiceServlet extends ChooselServiceServlet implements
        FeedbackService {

    private FeedbackService service = null;

    private FeedbackService getServiceDelegate() {
        if (service == null) {
            service = new DefaultFeedbackService(
                    UserServiceFactory.getUserService(),
                    MailServiceFactory.getMailService());
        }

        assert service != null;

        return service;
    }

    @Override
    public void sendFeedback(final String message, final String errorMessage)
            throws ServiceException {

        execute(new Task<Void>() {
            @Override
            public Void execute() throws ServiceException {
                getServiceDelegate().sendFeedback(message, errorMessage);
                return null;
            }
        });
    }
}
