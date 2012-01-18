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

import com.google.gwt.user.client.Window;

/**
 * Object-oriented wrapper for {@link Window.Location} that enables mock object
 * testing. Implemented in {@link DefaultWindowLocation}.
 * 
 * @author Lars Grammel
 * 
 * @see DefaultWindowLocation
 */
public interface WindowLocation {

    /**
     * @see Window.Location#getParameter(String)
     */
    String getParameter(String name);

    /**
     * @see Window.Location#getParameterMap()
     */
    Map<String, List<String>> getParameterMap();

}