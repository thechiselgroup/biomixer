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

import com.google.gwt.event.shared.GwtEvent;

public class NodeDragEvent extends GwtEvent<NodeDragHandler> {

    public static final Type<NodeDragHandler> TYPE = new Type<NodeDragHandler>();

    private final int endX;

    private final int endY;

    private final Node node;

    private final int startX;

    private final int startY;

    public NodeDragEvent(Node node, int startX, int startY, int endX, int endY) {
        assert node != null;

        this.node = node;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;

    }

    @Override
    protected void dispatch(NodeDragHandler handler) {
        handler.onDrag(this);
    }

    @Override
    public Type<NodeDragHandler> getAssociatedType() {
        return TYPE;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public Node getNode() {
        return node;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    @Override
    public String toString() {
        return "NodeDragEvent [endX=" + endX + ", endY=" + endY + ", node="
                + node + ", startX=" + startX + ", startY=" + startY + "]";
    }

}