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
package org.thechiselgroup.biomixer.client.visualization_component.text;

import org.thechiselgroup.biomixer.client.core.ui.CSS;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

public class DefaultTextItemLabel implements TextItemLabel {

    private Element element;

    public DefaultTextItemLabel() {
        this.element = DOM.createDiv();
    }

    @Override
    public void addStyleName(String cssClass) {
        assert cssClass != null;
        element.addClassName(cssClass);
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public String getText() {
        return element.getInnerText();
    }

    @Override
    public void registerHandler(EventListener eventListener) {
        DOM.sinkEvents(element, Event.MOUSEEVENTS | Event.ONCLICK);
        DOM.setEventListener(element, eventListener);
    }

    @Override
    public void removeStyleName(String cssClass) {
        assert cssClass != null;
        element.removeClassName(cssClass);
    }

    @Override
    public void setFontSize(String fontSize) {
        CSS.setFontSize(getElement(), fontSize);
    }

    @Override
    public void setText(String text) {
        assert text != null;
        element.setInnerText(text);
    }

}