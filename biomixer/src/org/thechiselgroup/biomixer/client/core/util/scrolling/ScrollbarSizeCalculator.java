/*******************************************************************************
 * Copyright 2012 David Rusk 
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
package org.thechiselgroup.biomixer.client.core.util.scrolling;

import com.google.gwt.core.client.JsArrayInteger;

/**
 * Measures HTML/CSS scrollbar thickness. This value is not constant across
 * browsers.
 * 
 * @author drusk
 * 
 */
public class ScrollbarSizeCalculator {

    public int getHorizontalScrollbarThickness() {
        return getScrollSize().get(1);
    }

    private native JsArrayInteger getScrollSize() /*-{
		var el = $doc.createElement('div');
		el.style.display = 'hidden';
		el.style.overflow = 'scroll';
		$doc.body.appendChild(el);
		var w = el.offsetWidth - el.clientWidth;
		var h = el.offsetHeight - el.clientHeight;
		$doc.body.removeChild(el);
		return new Array(w, h);
    }-*/;

    public int getVerticalScrollbarThickness() {
        return getScrollSize().get(0);
    }

}
