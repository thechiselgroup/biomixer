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
package org.thechiselgroup.biomixer.client.visualization_component.graph.widget;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public abstract class ArcEvent<T extends EventHandler> extends GwtEvent<T> {

    private final Arc arc;

    private final int mouseX;

    private final int mouseY;

    public ArcEvent(Arc arc, int mouseX, int mouseY) {
        this.arc = arc;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public Arc getArc() {
        return arc;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    @Override
    public String toString() {
        return "ArcEvent [arc=" + arc + ", mouseX=" + mouseX + ", mouseY="
                + mouseY + "]";
    }

}