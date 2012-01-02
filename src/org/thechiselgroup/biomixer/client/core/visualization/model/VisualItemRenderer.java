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

import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;

// TODO pull up slots into this?
public interface VisualItemRenderer {

    /**
     * <p>
     * Updates the {@link ViewContentDisplay}. There is no overlap between the
     * three different {@link VisualItem} sets in the {@link Delta} (added,
     * updated, and removed {@link VisualItem}s). We use a single method to
     * enable the different {@link ViewContentDisplay}s to do a single refresh
     * of the view instead of multiple operations.
     * </p>
     * <p>
     * The {@link VisualItem}s can be referenced during a session for reference
     * testing. When a {@link VisualItem} is created, it is passed in as part of
     * the added {@link VisualItem}s, when it changes, it is part of the updated
     * {@link VisualItem}s, and when it is removed, it is part of the removed
     * {@link VisualItem}s.
     * </p>
     * <p>
     * In addition to changing {@link VisualItem}s, the slot mapping of a
     * visualization could have changed in the same instance. This is reflected
     * by the changedSlots parameter.
     * </p>
     * <p>
     * <b>IMPORTANT:</b> The caller should guarantee that all {@link Slot}s can
     * always be resolved on the {@link VisualItem}s in the
     * {@link VisualItemRenderer}. If a {@link VisualItem} cannot resolve all
     * slots any more, it should be removed.
     * </p>
     * 
     * @param delta
     *            {@link VisualItem} delta that contains (a) {@link VisualItem}s
     *            that have been added to the view, (b) {@link VisualItem}s
     *            which have changed (status, data, etc.) such that their
     *            representation needs to be updated, and (c) {@link VisualItem}
     *            s that have been removed from the view.
     * @param updatedSlots
     *            {@link Slot}s for which the mappings have changed. Is never
     *            <code>null</code>, but can be an empty set.
     */
    void update(Delta<VisualItem> delta,
            LightweightCollection<Slot> updatedSlots);

}