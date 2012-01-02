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
package org.thechiselgroup.biomixer.client.workbench.init;

import java.util.List;
import java.util.Map;

import org.thechiselgroup.choosel.core.client.error_handling.ErrorHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ChooselApplicationInitializer implements ApplicationInitializer {

    /**
     * URL parameter for the application mode. The application mode is used to
     * switch between different initializers.
     */
    public static final String APPLICATION_MODE_PARAMETER = "mode";

    public static final String WORKBENCH = "workbench";

    public static final String EMBED = "embed";

    @Inject
    protected ErrorHandler errorHandler;

    @Inject
    @Named(WORKBENCH)
    private ApplicationInitializer workbenchInitializer;

    @Inject
    @Named(EMBED)
    private ApplicationInitializer embedInitializer;

    @Inject
    private WindowLocation windowLocation;

    @Override
    public void init() throws Exception {
        initGlobalErrorHandler();
        initApplicationMode();
    }

    private void initApplicationMode() throws Exception {
        Map<String, List<String>> parameters = windowLocation.getParameterMap();

        if (!parameters.containsKey(APPLICATION_MODE_PARAMETER)) {
            workbenchInitializer.init();
            return;
        }

        List<String> applicationModes = parameters
                .get(APPLICATION_MODE_PARAMETER);

        if (applicationModes.size() != 1) {
            throw new InitializationException("invalid number of values for "
                    + APPLICATION_MODE_PARAMETER + " parameter (was: "
                    + applicationModes.size() + " - " + applicationModes + ")");
        }

        String applicationMode = applicationModes.get(0);

        if (WORKBENCH.equals(applicationMode)) {
            workbenchInitializer.init();
        } else if (EMBED.equals(applicationMode)) {
            embedInitializer.init();
        } else {
            throw new InitializationException("invalid value for "
                    + APPLICATION_MODE_PARAMETER + " parameter (was: "
                    + applicationMode + ")");
        }
    }

    private void initGlobalErrorHandler() {
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                errorHandler.handleError(e);
            }
        });
    }

}