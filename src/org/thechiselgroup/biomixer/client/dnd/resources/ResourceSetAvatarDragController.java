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
package org.thechiselgroup.biomixer.client.dnd.resources;

import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.google.gwt.user.client.ui.Widget;

public interface ResourceSetAvatarDragController {

    void addDragHandler(DragHandler handler);

    void registerDropController(ResourceSetAvatarDropController dropController);

    void removeDragHandler(DragHandler handler);

    void setDraggable(Widget widget, boolean draggable);

    void unregisterDropController(ResourceSetAvatarDropController dropController);

    void unregisterDropControllerFor(Widget dropTarget);

}