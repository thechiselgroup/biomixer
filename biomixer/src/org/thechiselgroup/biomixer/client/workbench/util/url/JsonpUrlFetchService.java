/*******************************************************************************
 * Copyright 2012 David Rusk, Elena Voyloshnikova 
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
package org.thechiselgroup.biomixer.client.workbench.util.url;

import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Retrieves the content of URLs using JSONP.
 */
public class JsonpUrlFetchService implements UrlFetchService {

    @Override
    public void fetchURL(final String url, final AsyncCallback<String> callback) {

        JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
        jsonp.setTimeout(60000);
        jsonp.requestObject(url, new AsyncCallback<JavaScriptObject>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(JavaScriptObject result) {
                callback.onSuccess(new JSONObject(result).toString());
            }

        });

    }

}
