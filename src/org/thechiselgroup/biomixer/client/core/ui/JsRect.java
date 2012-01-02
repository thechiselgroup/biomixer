/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.core.ui;

import com.google.gwt.core.client.JavaScriptObject;

public class JsRect extends JavaScriptObject {

    protected JsRect() {
    }

    // @formatter:off
    public final native int getBottom() /*-{
        return this.bottom;
    }-*/;
    // @formatter:on

    // @formatter:off
    public final native int getLeft() /*-{
        return this.left;
    }-*/;
    // @formatter:on

    // @formatter:off
    public final native int getRight() /*-{
        return this.right;
    }-*/;
    // @formatter:on

    // @formatter:off
    public final native int getTop() /*-{
        return this.top;
    }-*/;
    // @formatter:on

}