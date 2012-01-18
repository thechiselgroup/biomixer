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

public class BenchmarkTaskExecutor extends DelegatingTaskExecutor {

    private final Logger logger;

    private final Level logLevel;

    public BenchmarkTaskExecutor(TaskExecutor delegate, Logger logger,
            Level logLevel) {

        super(delegate);

        assert logger != null;
        assert logLevel != null;

        this.logger = logger;
        this.logLevel = logLevel;
    }

    @Override
    public <T> T execute(Task<T> task) throws Exception {
        long startTime = System.currentTimeMillis();

        logger.log(logLevel, "start executing task " + task);
        try {
            return super.execute(task);
        } catch (Exception e) {
            logger.log(logLevel, "executing task " + task + " failed after "
                    + (System.currentTimeMillis() - startTime) + " ms");
            throw e;
        } finally {
            logger.log(logLevel, "finished executing task " + task + " in "
                    + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

}
