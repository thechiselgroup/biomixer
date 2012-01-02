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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.thechiselgroup.biomixer.client.workbench.DefaultBranding;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.WorkspaceDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.service.WorkspaceSharingService;
import org.thechiselgroup.biomixer.server.workbench.server.ChooselServiceServlet;
import org.thechiselgroup.biomixer.server.workbench.server.PMF;
import org.thechiselgroup.biomixer.server.workbench.util.PasswordGenerator;
import org.thechiselgroup.choosel.core.client.util.ServiceException;
import org.thechiselgroup.choosel.core.client.util.task.Task;

import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.users.UserServiceFactory;

public class WorkspaceSharingServiceServlet extends ChooselServiceServlet
        implements WorkspaceSharingService {

    // TODO move
    public static String constructURL(String path, HttpServletRequest request,
            ServletContext servletContext) {

        String scheme = request.getScheme();
        String server = request.getServerName();
        int port = request.getServerPort();
        // starts with /, ends without /
        String contextPath = servletContext.getContextPath();

        return scheme + "://" + server + ":" + port + contextPath + "/" + path;
    }

    private WorkspaceSharingService service = null;

    private String constructURL(String servlet) {
        HttpServletRequest request = perThreadRequest.get();
        ServletContext servletContext = getServletContext();

        return constructURL(servlet, request, servletContext);
    }

    private WorkspaceSharingService getServiceDelegate()
            throws NoSuchAlgorithmException {

        if (service == null) {
            // TODO inject branding
            service = new WorkspaceSharingServiceImplementation(PMF.get(),
                    new WorkspaceSecurityManager(UserServiceFactory
                            .getUserService()),
                    UserServiceFactory.getUserService(),
                    MailServiceFactory.getMailService(), new PasswordGenerator(
                            SecureRandom.getInstance("SHA1PRNG")),
                    constructURL("acceptInvitation"), new DefaultBranding());
        }

        return service;
    }

    @Override
    public void shareWorkspace(final WorkspaceDTO workspaceDTO,
            final String emailAddress) throws ServiceException {

        execute(new Task<Void>() {
            @Override
            public Void execute() throws ServiceException,
                    NoSuchAlgorithmException {
                getServiceDelegate().shareWorkspace(workspaceDTO, emailAddress);
                return null;
            }
        });
    }
}