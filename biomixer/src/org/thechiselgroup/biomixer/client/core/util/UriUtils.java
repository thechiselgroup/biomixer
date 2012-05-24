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
package org.thechiselgroup.biomixer.client.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.gwt.core.client.GWT;

/**
 * 
 * Provides access to JavaScript URI-related functionality.
 * 
 * @author Lars Grammel
 */
public final class UriUtils {

    public static String decodeURIComponent(String uriComponent) {
        if (GWT.isClient()) {
            return nativeDecodeURIComponent(uriComponent);
        }

        // XXX does not do anything
        return uriComponent;
    }

    /**
     * Similar to JavaScript {@code encodeURIComponent()} function.
     * 
     * @see http://xkr.us/articles/javascript/encode-compare/ for details on why
     *      to prefer {@code encodeURIComponent()} over {@code encode()} and
     *      {@code encodeURI()}.
     */
    public static String encodeURIComponent(String uriComponent) {
        if (GWT.isClient()) {
            return nativeEncodeURIComponent(uriComponent);
        } else {
            try {
                return URLEncoder.encode(uriComponent, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    // @formatter:off
    private static native String nativeDecodeURIComponent(String uriComponent) /*-{
		return decodeURIComponent(uriComponent);
    }-*/;
    // @formatter:on

    // @formatter:off
    private static native String nativeEncodeURIComponent(String uriComponent) /*-{
		return encodeURIComponent(uriComponent);
    }-*/;
    // @formatter:on

    private UriUtils() {
    }
}
