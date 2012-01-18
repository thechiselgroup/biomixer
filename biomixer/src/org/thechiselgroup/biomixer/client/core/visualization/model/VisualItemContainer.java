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

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Read-only container of {@link VisualItem}s.
 * 
 * @author Lars Grammel
 * 
 * @see VisualItem
 */
public interface VisualItemContainer {

    /**
     * @param handler
     *            Handler that will be notified whenever the {@link VisualItem}s
     *            in this container change.
     * 
     * @return {@link HandlerRegistration} that can used to remove
     *         {@code handler} from this container.
     */
    HandlerRegistration addHandler(VisualItemContainerChangeEventHandler handler);

    /**
     * @return <code>true</code>, if there is a {@link VisualItem} with the
     *         specified <code>visualItemId</code> in this container.
     */
    boolean containsVisualItem(String visualItemId);

    /**
     * @return {@link VisualItem} with the given ID.
     * 
     * @throws AssertionError
     *             thrown if there no view item with {@code visualItemId}
     */
    VisualItem getVisualItem(String visualItemId);

    /**
     * @return All {@link VisualItem}s in this container.
     */
    LightweightCollection<VisualItem> getVisualItems();

    /**
     * @return {@link VisualItem}s that contain at least one of the given
     *         {@link Resource}s.
     */
    LightweightCollection<VisualItem> getVisualItems(
            Iterable<Resource> resources);

}