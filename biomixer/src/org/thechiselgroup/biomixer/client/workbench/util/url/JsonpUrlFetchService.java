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

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.error_handling.RetryAsyncCallbackErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.json.JsJsonParser;
import org.thechiselgroup.biomixer.client.services.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Retrieves the content of URLs using JSONP.
 */
public class JsonpUrlFetchService implements UrlFetchService {

    @Override
    public void fetchURL(final String url, final AsyncCallback<String> callback) {
        fetchURL(url, callback, 0);
    }

    /**
     * This is primarily used by the {@link RetryAsyncCallbackErrorHandler} that
     * this uses internally.
     * 
     * @param url
     * @param callback
     * @param previousNumberTries
     */
    public void fetchURL(final String url,
            final AsyncCallback<String> callback, int previousNumberTries) {
        JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
        // Could change timeout, but probably better to change retry attempt
        // number...except that exacerbates server load. Maybe longer timeout is
        // ok.
        jsonp.setTimeout(jsonp.getTimeout() * 4);

        jsonp.requestObject(url,
                new ErrorHandlingAsyncCallback<JavaScriptObject>(
                        new RetryAsyncCallbackErrorHandler(callback, url,
                                previousNumberTries, this)) {

                    @Override
                    protected void runOnSuccess(JavaScriptObject result)
                            throws Exception {
                        // Had trouble with injection...explicitly creating
                        // instead.
                        ErrorCodeJSONParser errorCodeParser = new ErrorCodeJSONParser(
                                new JsJsonParser());

                        JSONObject jsonObject = new JSONObject(result);
                        String jsonString = jsonObject.toString();

                        // Need to check for understood errors in response, such
                        // as 403 forbidden.

                        Integer errorCode = errorCodeParser.parse(jsonString);

                        if (null != errorCode && 500 == errorCode) {
                            // 500 errors don't get here! Caught lower down?
                            // We can retry this once, since I have already seen
                            // cases of very singular failures here.
                            boolean retryAttempted = ((RetryAsyncCallbackErrorHandler) callback)
                                    .manualRetry();
                            if (retryAttempted) {
                                return;
                            }
                            // else if (403 == errorCode) {
                            // // This error code, forbidden, is something I
                            // want
                            // // to ignore at the moment.
                            // return;
                            // }
                        } else { // if (null == errorCode) {
                            callback.onSuccess(jsonString);
                            return;
                        }

                        // This wasn't a success, and we got an error code
                        // we don't understand.
                        // Treat as an error for the callback.
                        callback.onFailure(new Exception("Error code, status: "
                                + errorCode + "."));
                        throw new Exception("Status " + errorCode);

                    }

                });

    }

    private class ErrorCodeJSONParser extends AbstractJsonResultParser {

        @Inject
        public ErrorCodeJSONParser(JsonParser jsonParser) {
            super(jsonParser);
        }

        @Override
        public Integer parse(String json) {
            Integer errorCode = null;
            // Grab possible error code in status...
            // Like:
            /*
             * __gwt_jsonp__.P241.onSuccess( { "status": 403, "body":
             * "{\"errorStatus\": {\
             * "accessedResource\":\"\\\/bioportal\\\/ontologies\\\/metrics\\\/45290\",\"accessDate\":\"2013-02-18
             * 10:38:06.202
             * PST\",\"shortMessage\":\"Forbidden\",\"longMessage\":\"This
             * ontology is either private or licensed. Please go to
             * http:\\\/\\\/
             * bioportal.bioontology.org\\\/ontologies\\\/1578?p=terms to get
             * access to the ontology.\",\"errorCode\":403}}" } );
             */
            try {
                errorCode = asInt(get(super.parse(json), "status"));
            } catch (Exception e) {
                // I may get an exception if there is no such "status" entry.
                // Skip it. We parse the response in a callback later, so we can
                // let things be here. All we want is the error code if it
                // exists.
            }

            return errorCode;
        }

    }
}
