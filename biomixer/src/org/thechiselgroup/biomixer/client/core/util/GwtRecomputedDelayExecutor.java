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
import org.thechiselgroup.biomixer.shared.core.util.ConditionWithDelay;
import org.thechiselgroup.biomixer.shared.core.util.DelayedExecutor;

import com.google.gwt.user.client.Timer;

/**
 * The goal of this class is to create a timed queue, so that if multiple
 * requests to perform the same expensive action come near eachother, they will
 * be conglomerated. This is an older approach one can use with an autocompleted
 * field on a web page.
 * 
 * The condition passed in originally needs to have its requet time condition
 * updated in order to affect your Executor instance. Keep track of both in he
 * calling code, and update the {@link ConditionWithDelay#setRequestTime(long)}
 * whenever a new request occurs, while the Executor ahs not acted, or call
 * execute() again if it has acted. Call
 * {@link GwtRecomputedDelayExecutor#canMakeNewRequest()} to see if the Executor
 * has acted yet.
 * 
 * To use this class properly, do not call execute() when it your Runnable has
 * not actually been called yet. Otherwise, you will spawn more timers than you
 * intend to. If you are using another runnable, make another executor, since
 * there are instance variables that you probably don't want to share from your
 * single Executor onto two unrelated Runnable instances.
 * 
 */
public class GwtRecomputedDelayExecutor implements DelayedExecutor {

    private boolean ran = true;

    public boolean canMakeNewRequest() {
        return ran;
    }

    @Override
    public void execute(final Runnable code, final Condition condition,
            int retryDelayInMs) {
        ran = false;

        if (condition.isMet()) {
            ran = true;
            code.run();
            return;
        }

        if (condition instanceof ConditionWithDelay) {
            // If this was used with a normal Condition, just use original
            // delay again and again. Otherwise, update it to a reasonable
            // time.
            // Add +1 so that we don't have an issue with being off by a mere
            // millisecond on the condition check later.
            retryDelayInMs = ((ConditionWithDelay) condition).getDelay() + 1;
        }

        final int delay = retryDelayInMs;

        new Timer() {
            @Override
            public void run() {
                execute(code, condition, delay);
            }
        }.schedule(retryDelayInMs);
    }

    @Override
    public void execute(final Runnable code, int delayInMs) {
        ran = false;
        new Timer() {
            @Override
            public void run() {
                ran = true;
                code.run();
            }
        }.schedule(delayInMs);
    }

}