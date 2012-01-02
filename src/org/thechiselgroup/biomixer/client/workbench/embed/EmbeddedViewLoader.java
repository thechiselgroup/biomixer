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
package org.thechiselgroup.biomixer.client.workbench.embed;

import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;
import org.thechiselgroup.choosel.core.client.visualization.View;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EmbeddedViewLoader {

    /**
     * @return value that needs to be set as
     *         {@link EmbedInitializer#EMBED_MODE_PARAMETER} to use this loader.
     */
    String getEmbedMode();

    void loadView(WindowLocation windowLocation, AsyncCallback<View> callback);

}