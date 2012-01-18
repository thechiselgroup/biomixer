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
package org.thechiselgroup.biomixer.client.core.ui.shade;

import static com.google.gwt.user.client.DOM.createDiv;
import static org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants.ROOT_PANEL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.thechiselgroup.biomixer.client.core.geometry.Rectangle;
import org.thechiselgroup.biomixer.client.core.ui.CSS;
import org.thechiselgroup.biomixer.client.core.ui.ZIndex;
import org.thechiselgroup.biomixer.client.core.util.RemoveHandle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ShadeManager implements HasClickHandlers {

    public static final String CSS_SHADE = "shade";

    private final EventListener eventForwarder = new EventListener() {

        @Override
        public void onBrowserEvent(Event event) {
            switch (event.getTypeInt()) {
            case Event.ONCLICK:
                fireEvent(new ClickEvent() {
                });
                break;
            }
        }
    };

    private final HandlerManager handlerManager = new HandlerManager(this);

    private final AbsolutePanel panel;

    public final List<Element> shadedElements = new ArrayList<Element>();

    private Set<Object> tokens = new HashSet<Object>();

    private final int zIndex;

    private final String position;

    @Inject
    public ShadeManager(@Named(ROOT_PANEL) AbsolutePanel panel) {
        this(panel, ZIndex.SHADE, CSS.ABSOLUTE);
    }

    public ShadeManager(AbsolutePanel panel, int zIndex, String position) {
        this.panel = panel;
        this.zIndex = zIndex;
        this.position = position;
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return handlerManager.addHandler(ClickEvent.getType(), handler);
    }

    private Element createShadeElement(Rectangle r) {
        Element shadeElement = createDiv();

        shadeElement.setClassName(ShadeManager.CSS_SHADE);

        CSS.setPosition(shadeElement, position);
        CSS.setLeft(shadeElement, r.getX());
        CSS.setTop(shadeElement, r.getY());
        CSS.setHeight(shadeElement, r.getHeight());
        CSS.setWidth(shadeElement, r.getWidth());
        CSS.setZIndex(shadeElement, zIndex);

        return shadeElement;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        handlerManager.fireEvent(event);
    }

    private void hideShade() {
        for (Element e : shadedElements) {
            panel.getElement().removeChild(e);
        }
        shadedElements.clear();
    }

    public RemoveHandle showShade() {
        return showShade(Collections.<Rectangle> emptyList());
    }

    public RemoveHandle showShade(List<Rectangle> excludedAreas) {
        /*
         * Token-based mechanism that supports acquiring a shade multiple times.
         * The shade is removed when all tokens are released.
         */

        if (tokens.isEmpty()) {
            List<Rectangle> shadedRectangles = Rectangle.fromWidget(panel)
                    .calculateRemainder(excludedAreas);

            for (Rectangle r : shadedRectangles) {
                shadedElements.add(createShadeElement(r));
            }

            for (Element e : shadedElements) {
                Event.setEventListener(e, eventForwarder);
                Event.sinkEvents(e, Event.ONCLICK);
                panel.getElement().appendChild(e);
            }
        }

        final Object token = new Object();
        tokens.add(token);

        return new RemoveHandle() {
            @Override
            public void remove() {
                tokens.remove(token);
                if (tokens.isEmpty()) {
                    hideShade();
                }
            }
        };
    }

}
