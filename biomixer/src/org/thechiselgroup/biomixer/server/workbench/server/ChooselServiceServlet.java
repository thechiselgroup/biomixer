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
package org.thechiselgroup.biomixer.server.workbench.server;

import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.thechiselgroup.biomixer.client.core.util.ServiceException;
import org.thechiselgroup.biomixer.client.core.util.task.DirectTaskExecutor;
import org.thechiselgroup.biomixer.client.core.util.task.LogErrorTaskExecutor;
import org.thechiselgroup.biomixer.client.core.util.task.Task;
import org.thechiselgroup.biomixer.client.core.util.task.TaskExecutor;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ChooselServiceServlet extends RemoteServiceServlet {

    private TaskExecutor taskExecutor;

    protected <T> T execute(Task<T> task) throws ServiceException {
        try {
            return taskExecutor.execute(task);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        taskExecutor = new LogErrorTaskExecutor(new DirectTaskExecutor(),
                Logger.getLogger(getClass().getName()));
    }

}