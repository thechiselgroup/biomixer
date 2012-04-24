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
package org.thechiselgroup.biomixer.client.core.util.executor;

import com.google.gwt.user.client.Timer;

public class GwtDelayedExecutor implements DelayedExecutor {

    private int delay;

    /**
     * Creates a delayed executor with no delay.
     */
    public GwtDelayedExecutor() {
        this(0);
    }

    /**
     * Creates a delayed executor with a specified delay in milliseconds.
     * 
     * @param delay
     *            delay in milliseconds
     */
    public GwtDelayedExecutor(int delay) {
        this.delay = delay;
    }

    @Override
    public void execute(final Runnable command) {
        new Timer() {
            @Override
            public void run() {
                command.run();
            }
        }.schedule(delay);
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
    }

}
