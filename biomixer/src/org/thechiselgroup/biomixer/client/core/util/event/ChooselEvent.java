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
package org.thechiselgroup.biomixer.client.core.util.event;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.Event;

/**
 * User interaction with a screen element. This should wrap mouse and
 * potentially keyboard interaction. A separate class was introduced to
 * facilitate the integration of components using 3rd party technology, e.g.
 * Flash, Silverlight.
 */
public class ChooselEvent {

    /**
     * Separate event types are used to enable simulation of events that are not
     * supported by the GWT event library (as of GWT 2.1), e.g. HTML5 events
     * such as DRAG_START.
     */
    public static enum Type {

        CLICK(Event.ONCLICK), DOUBLE_CLICK(Event.ONDBLCLICK), CONTEXT_MENU(
                Event.ONCONTEXTMENU), MOUSE_OVER(Event.ONMOUSEOVER), MOUSE_MOVE(
                Event.ONMOUSEMOVE), MOUSE_OUT(Event.ONMOUSEOUT), MOUSE_DOWN(
                Event.ONMOUSEDOWN), MOUSE_UP(Event.ONMOUSEUP), MOUSE_WHEEL(
                Event.ONMOUSEWHEEL), TOUCH_START(Event.ONTOUCHSTART), TOUCH_END(
                Event.ONTOUCHEND), TOUCH_MOVE(Event.ONTOUCHMOVE), TOUCH_CANCEL(
                Event.ONTOUCHCANCEL), GESTURE_START(Event.ONGESTURESTART), GESTURE_CHANGE(
                Event.ONGESTURECHANGE), GESTURE_END(Event.ONGESTUREEND), DRAG_START, DRAG_END, UNDEFINED;

        private static final Map<Integer, Type> typesByCode = new HashMap<Integer, ChooselEvent.Type>();

        static {
            Type[] values = values();
            for (Type type : values) {
                if (type.getEventCode() != -1) {
                    typesByCode.put(type.getEventCode(), type);
                }
            }
        }

        /**
         * @return Type or {@link #UNDEFINED}, if there is no such event.
         * 
         * @see Event
         */
        private static Type byCode(int eventCode) {
            if (!typesByCode.containsKey(eventCode)) {
                return UNDEFINED;
            }
            return typesByCode.get(eventCode);
        }

        /**
         * Event code from {@link Event}, if available, -1 otherwise.
         */
        private int eventCode;

        private Type() {
            this(-1);
        }

        private Type(int eventCode) {
            this.eventCode = eventCode;
        }

        public int getEventCode() {
            return eventCode;
        }

    }

    /**
     * Unknown coordinate.
     * 
     * @see #getClientX()
     * @see #getClientY()
     */
    public final static int UNKNOWN = -2;

    private Event browserEvent;

    private final int clientX;

    private final int clientY;

    private final Type eventType;

    public ChooselEvent(Event e) {
        assert e != null;

        this.browserEvent = e;
        // This next call is almost certainly a horrid mistake...but it seems to
        // do really well at preventing text selection in Chrome when I don't
        // want to select text! Let's try it out here. It can be pushed lower
        // into listeners later if necessary, after forcing the native event
        // into all ChooselEvent constructors.
        e.preventDefault();
        this.clientX = e.getClientX();
        this.clientY = e.getClientY();
        this.eventType = Type.byCode(Event.as(e).getTypeInt());
    }

    public ChooselEvent(Event e, Type eventType, int clientX, int clientY) {
        // For testing, when we will indeed use a null event. Maybe useful
        // elsewhere? I doubt it!
        this.eventType = eventType;
        this.clientX = clientX;
        this.clientY = clientY;
    }

    /**
     * Browser event, if available.
     */
    public Event getBrowserEvent() {
        return browserEvent;
    }

    /**
     * X-Coordinate of the event in the browser client area.Can be
     * {@link #UNKNOWN}.
     */
    public int getClientX() {
        return clientX;
    }

    /**
     * Y-Coordinate of the event in the browser client area. Can be
     * {@link #UNKNOWN}.
     */
    public int getClientY() {
        return clientY;
    }

    /**
     * Type of this event.
     * 
     * @see Type
     */
    public Type getEventType() {
        return eventType;
    }

    public boolean hasBrowserEvent() {
        return browserEvent != null;
    }

    @Override
    public String toString() {
        return eventType + " (" + clientX + "," + clientY + ")";
    }

}