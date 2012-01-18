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
package org.thechiselgroup.biomixer.client.visualization_component.timeline;

import org.thechiselgroup.biomixer.client.core.util.collections.ArrayUtils;

import com.google.gwt.core.client.JavaScriptObject;

public class JsTimeLineEventSource extends JavaScriptObject {

    public static native JsTimeLineEventSource create() /*-{
        return new $wnd.Timeline.DefaultEventSource();
    }-*/;

    protected JsTimeLineEventSource() {
    }

    private final native void addEvents(JavaScriptObject events) /*-{
        this.addMany(events);
    }-*/;

    public final void addEvents(JsTimeLineEvent[] events) {
        addEvents(ArrayUtils.toJsArray(events));
    }

    // @formatter:off
    private final native void removeEvents(JavaScriptObject events) /*-{
        // the event index class does not support remove, so we hack it in...
        for (var i = 0; i < events.length; i++) {
            this._events._events.remove(events[i]);
            delete this._events._idToEvent[events[i].getID()];
        }

        this._events._indexed = false;

        // XXX event source has no remove method, not sure which event to fire
        this._fire("onAddMany", []);
    }-*/;
    // @formatter:on

    public final void removeEvents(JsTimeLineEvent[] events) {
        removeEvents(ArrayUtils.toJsArray(events));
    }

}