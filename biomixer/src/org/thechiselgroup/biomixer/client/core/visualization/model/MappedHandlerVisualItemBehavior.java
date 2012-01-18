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
package org.thechiselgroup.biomixer.client.core.visualization.model;

import static org.thechiselgroup.biomixer.client.core.util.DisposeUtil.safelyDispose;

import java.util.Map;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;

public abstract class MappedHandlerVisualItemBehavior<T> implements
        VisualItemBehavior {

    /**
     * Maps visual item ids to handlers.
     */
    private final Map<String, T> mappedHandlers = CollectionFactory
            .createStringMap();

    private ErrorHandler errorHandler;

    private void assertContainsVisualItem(VisualItem visualItem) {
        assert visualItem != null;
        assert mappedHandlers.containsKey(visualItem.getId()) : "no handler "
                + "registered for visual item  with id " + visualItem.getId();
    }

    private void assertDoesNotContainVisualItem(VisualItem visualItem) {
        assert visualItem != null;
        assert !mappedHandlers.containsKey(visualItem.getId()) : "visual item with id "
                + visualItem.getId() + " has a registered handler already";
    }

    protected abstract T createHandler(VisualItem visualItem);

    protected T getHandler(VisualItem visualItem) {
        return mappedHandlers.get(visualItem.getId());
    }

    @Override
    public final void onInteraction(VisualItem visualItem,
            VisualItemInteraction interaction) {

        assertContainsVisualItem(visualItem);
        assert interaction != null;

        T handler = getHandler(visualItem);

        onInteraction(visualItem, interaction, handler);
    }

    protected abstract void onInteraction(VisualItem visualItem,
            VisualItemInteraction interaction, T handler);

    @Override
    public void onVisualItemContainerChanged(
            VisualItemContainerChangeEvent event) {

        assert event != null;

        /*
         * Order is important: removed visual items and added visual items can
         * share the same id.
         * 
         * XXX is this a bug and we should ensure this does not happen??
         */
        for (VisualItem item : event.getDelta().getRemovedElements()) {
            onVisualItemRemoved(item);
        }
        for (VisualItem item : event.getDelta().getAddedElements()) {
            onVisualItemCreated(item);
        }
    }

    public void onVisualItemCreated(VisualItem visualItem) {
        assertDoesNotContainVisualItem(visualItem);
        mappedHandlers.put(visualItem.getId(), createHandler(visualItem));
    }

    public void onVisualItemRemoved(VisualItem visualItem) {
        assertContainsVisualItem(visualItem);
        safelyDispose(mappedHandlers.remove(visualItem.getId()), errorHandler);
    }

}