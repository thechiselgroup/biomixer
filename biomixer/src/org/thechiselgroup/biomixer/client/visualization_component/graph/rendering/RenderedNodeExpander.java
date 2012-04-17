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
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * The UI element which provides node expansion options.
 * 
 * @author drusk
 * 
 */
public interface RenderedNodeExpander {

    Node getNode();

    /**
     * 
     * @param optionId
     *            the identifier for which expansion option has been selected
     * @param handler
     *            the event handler to be triggered when this
     *            <code>optionId</code> has been selected
     */
    void setEventHandlerOnOption(String optionId, ChooselEventHandler handler);

    /**
     * Sets the background colour of an expansion option, for highlighting
     * purposes.
     * 
     * @param optionId
     *            the identifier for which expansion option is selected
     * @param color
     *            the colour to make the background of the selected option
     */
    void setOptionBackgroundColor(String optionId, String color);

}
