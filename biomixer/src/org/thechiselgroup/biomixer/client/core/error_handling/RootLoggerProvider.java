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
package org.thechiselgroup.biomixer.client.core.error_handling;

import java.util.logging.Logger;

import com.google.inject.Provider;

public class RootLoggerProvider implements Provider<LoggerProvider> {

    @Override
    public LoggerProvider get() {
        return new LoggerProvider() {

            private Logger logger = Logger.getLogger("");

            @Override
            public Logger getLogger() {
                return logger;
            }
        };
    }

}