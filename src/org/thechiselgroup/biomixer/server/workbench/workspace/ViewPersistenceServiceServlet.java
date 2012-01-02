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

import java.util.List;

import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ViewDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.dto.ViewPreviewDTO;
import org.thechiselgroup.biomixer.client.workbench.workspace.service.ViewPersistenceService;
import org.thechiselgroup.biomixer.server.workbench.server.ChooselServiceServlet;
import org.thechiselgroup.biomixer.server.workbench.server.PMF;
import org.thechiselgroup.choosel.core.client.util.ServiceException;
import org.thechiselgroup.choosel.core.client.util.task.Task;

import com.google.appengine.api.users.UserServiceFactory;

public class ViewPersistenceServiceServlet extends ChooselServiceServlet
        implements ViewPersistenceService {

    private ViewPersistenceService service = null;

    @Override
    public Long deleteView(final Long viewId) throws ServiceException {
        return execute(new Task<Long>() {
            @Override
            public Long execute() throws ServiceException {
                return getServiceDelegate().deleteView(viewId);
            }
        });
    }

    private ViewPersistenceService getServiceDelegate() {
        if (service == null) {
            service = new ViewPersistenceServiceImplementation(PMF.get(),
                    UserServiceFactory.getUserService());
        }

        return service;
    }

    @Override
    public ViewDTO loadView(final Long viewId) throws ServiceException {
        return execute(new Task<ViewDTO>() {
            @Override
            public ViewDTO execute() throws ServiceException {
                return getServiceDelegate().loadView(viewId);
            }
        });
    }

    @Override
    public List<ViewPreviewDTO> loadViewPreviews() throws ServiceException {
        return execute(new Task<List<ViewPreviewDTO>>() {
            @Override
            public List<ViewPreviewDTO> execute() throws ServiceException {
                return getServiceDelegate().loadViewPreviews();
            }
        });
    }

    @Override
    public Long saveView(final ViewDTO view) throws ServiceException {
        return execute(new Task<Long>() {
            @Override
            public Long execute() throws ServiceException {
                return getServiceDelegate().saveView(view);
            }
        });
    }
}
