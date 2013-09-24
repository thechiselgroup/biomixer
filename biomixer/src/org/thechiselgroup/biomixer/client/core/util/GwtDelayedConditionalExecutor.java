/*******************************************************************************
 * Copyright (C) 2012 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.util;

import org.thechiselgroup.biomixer.shared.core.util.Condition;
import org.thechiselgroup.biomixer.shared.core.util.DelayedExecutor;

import com.google.gwt.user.client.Timer;

public class GwtDelayedConditionalExecutor implements DelayedExecutor {

    @Override
    public void execute(final Runnable code, final Condition condition,
            final int retryDelayInMs) {

        if (condition.isMet()) {
            code.run();
            return;
        }

        new Timer() {
            @Override
            public void run() {
                execute(code, condition, retryDelayInMs);
            }
        }.schedule(retryDelayInMs);
    }

    @Override
    public void execute(final Runnable code, int delayInMs) {
        new Timer() {
            @Override
            public void run() {
                code.run();
            }
        }.schedule(delayInMs);
    }

}