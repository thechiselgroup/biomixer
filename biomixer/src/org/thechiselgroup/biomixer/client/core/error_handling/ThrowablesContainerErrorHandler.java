/*******************************************************************************
 * Copyright 2012 David Rusk 
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

public class ThrowablesContainerErrorHandler implements ErrorHandler {

    private final ThrowablesContainer throwablesContainer;

    public ThrowablesContainerErrorHandler(
            ThrowablesContainer throwablesContainer) {
        this.throwablesContainer = throwablesContainer;
    }

    public void addListener(ThrowablesContainerEventListener listener) {
        throwablesContainer.addListener(listener);
    }

    @Override
    public void handleError(Throwable error) {
        throwablesContainer.addThrowableCaught(new ThrowableCaught(error));
    }

    public void removeListener(ThrowablesContainerEventListener listener) {
        throwablesContainer.removeListener(listener);
    }

}
