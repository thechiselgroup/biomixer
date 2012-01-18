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
package org.thechiselgroup.biomixer.client.core.visualization.model.extensions;

import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetAddedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetChangedEventHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetRemovedEventHandler;

import com.google.gwt.event.shared.HandlerRegistration;

public interface SelectionModel {

    HandlerRegistration addEventHandler(ResourceSetActivatedEventHandler handler);

    HandlerRegistration addEventHandler(ResourceSetAddedEventHandler handler);

    HandlerRegistration addEventHandler(ResourceSetChangedEventHandler handler);

    HandlerRegistration addEventHandler(ResourceSetRemovedEventHandler handler);

    void addSelectionSet(ResourceSet selectionSet);

    boolean containsSelectionSet(ResourceSet resourceSet);

    ResourceSet getSelection();

    ResourceSet getSelectionProxy();

    void removeSelectionSet(ResourceSet selectionSet);

    void setSelection(ResourceSet newSelectionModel);

    /**
     * Switches the containment of a {@link ResourceSet}. If a resource set is
     * switched and some items are already contained, switch to full selection.
     * This allows for consistent behavior of partially highlighted UI elements.
     */
    void switchSelection(ResourceSet resources);

}