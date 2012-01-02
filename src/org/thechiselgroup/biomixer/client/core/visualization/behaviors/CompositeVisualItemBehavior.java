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
package org.thechiselgroup.biomixer.client.core.visualization.behaviors;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemBehavior;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainerChangeEvent;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;

public class CompositeVisualItemBehavior implements VisualItemBehavior {

    private List<VisualItemBehavior> behaviors = new ArrayList<VisualItemBehavior>();

    public boolean add(VisualItemBehavior e) {
        return behaviors.add(e);
    }

    @Override
    public void onInteraction(VisualItem visualItem, VisualItemInteraction interaction) {
        for (VisualItemBehavior behavior : behaviors) {
            behavior.onInteraction(visualItem, interaction);
        }
    }

    @Override
    public void onVisualItemContainerChanged(VisualItemContainerChangeEvent event) {
        for (VisualItemBehavior behavior : behaviors) {
            behavior.onVisualItemContainerChanged(event);
        }
    }

    public boolean remove(VisualItemBehavior e) {
        return behaviors.remove(e);
    }

}