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

import org.thechiselgroup.biomixer.client.core.command.AsyncCommand;
import org.thechiselgroup.biomixer.client.core.command.AsyncCommandExecutor;

import com.google.inject.Inject;

public class ErrorHandlingAsyncCommandExecutor implements AsyncCommandExecutor {

    private ErrorHandler errorHandler;

    @Inject
    public ErrorHandlingAsyncCommandExecutor(ErrorHandler errorHandler) {
        assert errorHandler != null;
        this.errorHandler = errorHandler;
    }

    @Override
    public void execute(AsyncCommand command) {
        command.execute(new ErrorHandlingAsyncCallback<Void>(errorHandler));
    }
}
