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

import java.io.IOException;

import org.thechiselgroup.biomixer.client.core.util.ServiceException;
import org.thechiselgroup.biomixer.client.workbench.feedback.FeedbackService;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailService.Message;
import com.google.appengine.api.users.UserService;

public class DefaultFeedbackService implements FeedbackService {

    // TODO use configuration file
    private static final String ADMIN = "lars.grammel@googlemail.com";

    private final MailService mailService;

    private final UserService userService;

    public DefaultFeedbackService(UserService userService,
            MailService mailService) {

        assert userService != null;
        assert mailService != null;

        this.userService = userService;
        this.mailService = mailService;
    }

    @Override
    public void sendFeedback(String message, String errorMessage)
            throws ServiceException {

        StringBuffer content = new StringBuffer();

        String sender;
        if (userService.isUserLoggedIn()) {
            sender = userService.getCurrentUser().getEmail();
        } else {
            sender = ADMIN;
        }

        content.append(message);

        if (errorMessage != null) {
            content.append("\n\n");
            content.append("Exception: " + errorMessage);
        }

        try {
            mailService.send(new Message(sender, ADMIN, "[BioMixer Feedback]",
                    content.toString()));
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }
}
