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

import com.google.gwt.core.client.JavaScriptObject;

// TODO remove TimeLineItem dependency --> use payload object instead
public class JsTimeLineEvent extends JavaScriptObject {

    // TODO document the expected date format
    // @formatter:off
    public static native JsTimeLineEvent create(String date, String text,
            String icon, TimeLineItem timeLineItem) /*-{
        var parseDateTimeFunction = 
        $wnd.Timeline.NativeDateUnit.getParser(null);

        var jsonEvent = {
        start: parseDateTimeFunction(date),
        instant: true,
        text: text,
        icon: icon,
        color: null,                                      
        textColor: null,
        classname: null,
        eventID: null,
        timeLineItem: timeLineItem 
        }

        var evt = new $wnd.Timeline.DefaultEventSource.Event(jsonEvent);

        evt._obj = jsonEvent;
        evt.getProperty = function(name) {
        return this._obj[name];
        };

        return evt;
    }-*/;


    protected JsTimeLineEvent() {
    }

    public final native String getID() /*-{
        return this.getID();
    }-*/;

    public final native TimeLineItem getTimeLineItem() /*-{
        return this.getProperty('timeLineItem');
    }-*/;

    public final native void setTickBackgroundColor(String color) /*-{
        this._tickBackgroundColor = color;
    }-*/;

    public final native void setTickZIndex(String zIndex) /*-{
        this._tickZIndex = zIndex;
    }-*/;
    // @formatter:on

}
