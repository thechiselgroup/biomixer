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

public class ArcMouseDoubleClickEvent extends
        ArcEvent<ArcMouseDoubleClickHandler> {

    public static final Type<ArcMouseDoubleClickHandler> TYPE = new Type<ArcMouseDoubleClickHandler>();

    public ArcMouseDoubleClickEvent(Arc arc, int mouseX, int mouseY) {
        super(arc, mouseX, mouseY);
    }

    @Override
    protected void dispatch(ArcMouseDoubleClickHandler handler) {
        handler.onMouseDoubleClick(this);
    }

    @Override
    public Type<ArcMouseDoubleClickHandler> getAssociatedType() {
        return TYPE;
    }

}