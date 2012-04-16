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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering;

import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;

/**
 * The UI element which provides node expansion options.
 * 
 * @author drusk
 * 
 */
public interface RenderedNodeExpander {

    /* XXX for testing */
    ChooselEventHandler getEventHandler(String optionId);

    void setEventHandlerOnOption(String optionId, ChooselEventHandler handler);

    void setOptionBackgroundColor(String optionId, String color);

}
