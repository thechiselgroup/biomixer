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
package org.thechiselgroup.biomixer.server.workbench.workspace;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jdo.PersistenceManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.workbench.authentication.AuthorizationException;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.WorkspaceDTO;
import org.thechiselgroup.biomixer.server.workbench.util.PasswordGenerator;
import org.thechiselgroup.choosel.dnd.client.windows.Branding;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailService.Message;
import com.google.appengine.api.users.UserService;

public class WorkspaceSharingServiceImplementationTest {

    private static final String BASEURL = "BASEURL";

    @Mock
    private MailService mailService;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private PersistenceManagerFactory persistenceManagerFactory;

    private WorkspaceSharingServiceImplementation underTest;

    @Mock
    private PersistentSharingInvitation invitation;

    @Mock
    private UserService userService;

    @Mock
    private WorkspaceSecurityManager workspaceSecurityManager;

    @Mock
    private Branding branding;

    private String emailAddress = "test@test.com";

    @Test
    public void emailSubject() throws Exception {
        WorkspaceDTO workspaceDTO = new WorkspaceDTO(new Long(1), "name");
        String applicationTitle = "applicationTitle--X";

        when(branding.getApplicationTitle()).thenReturn(applicationTitle);

        underTest.shareWorkspace(workspaceDTO, emailAddress);

        ArgumentCaptor<Message> argument = ArgumentCaptor
                .forClass(MailService.Message.class);
        verify(mailService, times(1)).send(argument.capture());
        Message message = argument.getValue();

        assertEquals(applicationTitle + " workspace '" + workspaceDTO.getName()
                + "'", message.getSubject());
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        underTest = new WorkspaceSharingServiceImplementation(
                persistenceManagerFactory, workspaceSecurityManager,
                userService, mailService, passwordGenerator, BASEURL, branding) {

            @Override
            protected PersistentSharingInvitation createWorkspaceSharingInvitation(
                    WorkspaceDTO workspaceDTO, String emailAddress)
                    throws AuthorizationException {
                return invitation;
            }

            @Override
            protected String getEmail() {
                return emailAddress;
            }
        };

    }
}