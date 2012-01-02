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
package org.thechiselgroup.biomixer.client.core.util;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Collect handlers to dispose them later.
 */
public class HandlerRegistrationSet implements Disposable {

    private Set<HandlerRegistration> handlerRegistrations = new HashSet<HandlerRegistration>();

    public void addHandlerRegistration(HandlerRegistration registration) {
        assert registration != null;

        handlerRegistrations.add(registration);
    }

    @Override
    public void dispose() {
        for (HandlerRegistration registration : handlerRegistrations) {
            registration.removeHandler();
        }
    }

}
