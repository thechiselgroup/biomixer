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
package org.thechiselgroup.biomixer.client.core.visualization.behaviors;

import java.util.logging.Logger;

import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;

public class VisualItemInteractionLogger implements VisualItemBehavior {

    private Logger logger;

    public VisualItemInteractionLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onInteraction(VisualItem visualItem, VisualItemInteraction interaction) {
        logger.info("onInteraction[Interaction=" + interaction + " ; VisualItem="
                + visualItem + "]");
    }

    @Override
    public void onVisualItemContainerChanged(VisualItemContainerChangeEvent event) {
    }

}