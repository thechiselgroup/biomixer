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
package org.thechiselgroup.biomixer.client.core.util.task;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogErrorTaskExecutor extends DelegatingTaskExecutor {

    private Logger logger;

    public LogErrorTaskExecutor(TaskExecutor delegate, Logger logger) {
        super(delegate);

        assert logger != null;
        this.logger = logger;
    }

    @Override
    public <T> T execute(Task<T> task) throws Exception {
        try {
            return super.execute(task);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
    }

}