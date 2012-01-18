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
package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.thechiselgroup.biomixer.client.core.command.AsyncCommand;
import org.thechiselgroup.biomixer.client.core.command.AsyncCommandExecutor;
import org.thechiselgroup.biomixer.client.core.util.callbacks.NullAsyncCallback;

import com.google.inject.Inject;

public class LoggingAsyncCommandExecutor implements AsyncCommandExecutor {

    private final Logger logger;

    @Inject
    public LoggingAsyncCommandExecutor(LoggerProvider logger) {
        assert logger != null;
        this.logger = logger.getLogger();
    }

    @Override
    public void execute(AsyncCommand command) {
        command.execute(new NullAsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, caught.getMessage(), caught);
            }
        });
    }

}