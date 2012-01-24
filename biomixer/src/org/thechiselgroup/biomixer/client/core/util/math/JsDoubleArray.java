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
package org.thechiselgroup.biomixer.client.core.util.math;

import com.google.gwt.core.client.JsArrayNumber;

public final class JsDoubleArray extends JsArrayNumber implements NumberArray {

    public static native NumberArray create() /*-{
                                              return new Array();
                                              }-*/;

    protected JsDoubleArray() {
    }

    @Override
    public final native boolean isEmpty() /*-{
                                          return this.length === 0;
                                          }-*/;

    /**
     * Fast JavaScript max implementation.
     * 
     * @see {@link "http://www.javascriptrules.com/2009/09/23/fast-minmax-in-arrays/"}
     */
    @Override
    public native double max() /*-{
                               return Math.max.apply(null, this);
                               }-*/;

    /**
     * Fast JavaScript min implementation.
     * 
     * @see {@link "http://www.javascriptrules.com/2009/09/23/fast-minmax-in-arrays/"}
     */
    @Override
    public native double min() /*-{
                               return Math.min.apply(null, this);
                               }-*/;
}
